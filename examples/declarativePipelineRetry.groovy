#!/usr/bin/env groovy

/**
 * This is an example of declarative pipeline which will "retry" sh script
 * Please note that despite the fact of specifyed shebang line, this code can NOT be executed via groovy.
 *
 * Here some references used for build this example:
 * https://jenkins.io/doc/book/pipeline/syntax/#options
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#catcherror-catch-error-and-set-build-result-to-failure
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#retry-retry-the-body-up-to-n-times
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#timeout-enforce-time-limit
 * https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#waituntil-wait-for-condition
 * https://jenkins.io/doc/pipeline/steps/workflow-durable-task-step/#sh-shell-script
 */

pipeline {
    agent any
    options { timeout(time: 1, unit: 'DAYS') }
    stages {
        stage('retry') {
            steps {
                catchError(message: 'retry gave up, but we continue...', buildResult: 'SUCCESS', stageResult: 'SUCCESS'){
                    retry(count: 10) {
                        sh "exit 1"
                    }
                }
            }
        }
        stage('timeout + waitUntil') {
            options { timeout(time: 1, unit: 'HOURS') }
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitUntil {
                        executeShellScript 'exit 1'       
                    }
                }
            }
        }
    }
}
boolean executeShellScript(String script) {
    int status = sh script: script, returnStatus: true
    return status == 0
}