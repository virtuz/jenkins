#!/usr/bin/env groovy

/**
 * This is an example of declarative pipeline with Matcher.
 * Please note that despite the fact of specifyed shebang line, this code can NOT be executed via groovy.
 *
 * Here some references used for build this example:
 * https://docs.groovy-lang.org/latest/html/documentation/core-syntax.html#_groovydoc_comment
 * https://docs.groovy-lang.org/latest/html/documentation/core-operators.html#_find_operator
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
 * https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html
 * https://groovy-playground.appspot.com/
 * https://jenkins.io/doc/book/pipeline/syntax/#when
 */

pipeline {
    agent any
    parameters { 
        string(name: 'text', defaultValue: 'some text to match', description: 'some text')
        string(name: 'pattern', defaultValue: '^.*(match).*$', description: 'some pattern')
    }
    stages {
        stage('capture some value') {
            steps {
                echo "Looking for matches in text: '${params.text}', using the pattern '${params.pattern}'"
                script {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(params.pattern)
                    java.util.regex.Matcher matcher = pattern.matcher(params.text);
                    //java.util.regex.Matcher matcher = ( params.text =~ /^.*(match).*$/ )
                    if ( matcher.matches() ) {
                        env.VALUE = matcher.group(1)
                    }
                }
                echo "We captured ${env.VALUE}"
            }
        }
        stage('echo VALUE if captured'){
            when { expression { return env.VALUE } }
            steps {
                echo "As env variable exists we can print variable without prefix env., so VALUE is ${VALUE}"
            }
        }
    }
}