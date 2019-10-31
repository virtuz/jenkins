#!/usr/bin/env groovy

/**
 * This is an example of declarative pipeline which will "retry" sh script / http request
 * Please note that despite the fact of specifyed shebang line, this code can NOT be executed via groovy.
 *
 * Prerequisites:
 * HTTP Request plugin should be installed in Jenkins.
 * 
 * Here some references used for build this example:
 * https://jenkins.io/doc/book/pipeline/syntax/#options
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#catcherror-catch-error-and-set-build-result-to-failure
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#retry-retry-the-body-up-to-n-times
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#timeout-enforce-time-limit
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#waituntil-wait-for-condition
 * https://jenkins.io/doc/pipeline/steps/workflow-durable-task-step/#sh-shell-script
 * https://jenkins.io/doc/pipeline/steps/http_request/
 * https://jenkins.io/doc/book/managing/script-approval/
 * https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin
 */

pipeline {
    agent any
    options { 
        timeout(time: 1, unit: 'DAYS') 
        timestamps()
    }
    parameters { booleanParam(name: 'CATCH_INTERRUPTIONS', defaultValue: false, description: 'Used for catchError step for setting catchInterruptions') }
    stages {
        stage('waitUntil custom timeout') {
            steps {
                script {
                    long timeout = getUnixTime() +  5 * 1000
                    waitUntil {
                        isTimeoutReached(timeout)
                    }
                }
            }
        }
        stage('retry') {
            steps {
                echo "before catchError"
                catchError(message: 'retry gave up, but we continue...', buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                    retry(count: 5) {
                        echo "inside retry"
                        sh "exit 1"
                    }
                }
                echo "after catchError"
            }
        }
        stage('waitUntilSuccessfulResponse') {
            options { timeout(time: 1, unit: 'HOURS') }
            steps {
                script {
                    def response = waitUntilSuccessfulResponse 'http://localhost'
                    echo "final http status code is ${response?.status}"
                }
            }
        }
        stage('timeout + waitUntil + executeShellScript') {
            options { timeout(time: 1, unit: 'HOURS') }
            steps {
                echo "before catchError, catchInterruptions: ${params.CATCH_INTERRUPTIONS}"
                catchError(message: 'waitUntil gave up, but we continue...', catchInterruptions: params.CATCH_INTERRUPTIONS, buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                    timeout(time: 5, unit: 'SECONDS') {
                        waitUntil {
                            echo "inside waitUntil"
                            executeShellScript 'exit 1'       
                        }
                    }
                }
                echo "after catchError"
                echo "please note that catchInterruptions allow you to procced in case of timeout"
                echo "but build status became ABORTED and you are not able to change it back to SUCCESS"
            }
        }
    }
}

boolean executeShellScript(String script) {
    echo "executeShellScript(String script: ${script})"
    int status = sh script: script, returnStatus: true
    return status == 0
}

def waitUntilSuccessfulResponse(String url) {
    def response
    boolean isResponseStatusSuccessful = false
    long timeout = getUnixTime() + 5 * 1000
    echo "before catchError, catchInterruptions: ${params.CATCH_INTERRUPTIONS}"
    catchError(message: 'waitUntil gave up, but we continue...', catchInterruptions: params.CATCH_INTERRUPTIONS, buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
        waitUntil {
            response = httpRequest httpMode: 'GET', url: url, validResponseCodes: '100:999'
            echo "${url} has http status code ${response.status}"
            isResponseStatusSuccessful = ( response.status >= 200 && response.status < 400 )
            return isResponseStatusSuccessful || isTimeoutReached(timeout)
        }
    }
    echo "after catchError"
    return response
}

boolean isTimeoutReached(long timeout) {
    long now = getUnixTime()
    boolean isTimeoutReached = ( now > timeout )
    echo "now: ${now}, timeout: ${timeout}, isTimeoutReached: ${isTimeoutReached}"
    return isTimeoutReached
}

// In sandbox mode getting: Scripts not permitted to use method java.util.Date getTime. Administrators can decide whether to approve or reject this signature.
long getUnixTime() { 
    return new Date().getTime()
}