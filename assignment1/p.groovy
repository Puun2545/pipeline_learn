pipeline {
    agent none
    try {
        stages {
            stage('Build01') {
                agent 'slave-1'
                steps {
                    echo 'Starting the build Stage'
                    echo 'Build Stage completed successfully'
                }
            }
            stage('Build02') {
                agent 'slave-2'
                steps {
                    echo 'Starting the Deploy Stage'
                    echo 'Deploy Stage completed successfully'
                }
            }
        }
        catch (Exception e) {
            echo "Error: ${e}"
        }
        finally {
            echo 'Deployment completed successfully'
        }
    }
}