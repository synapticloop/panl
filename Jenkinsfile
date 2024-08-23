pipeline {
  agent any
  stages {
    stage('distribution') {
      steps {
        withGradle() {
          sh './gradlew assemble'
        }
      }
    }
  }

	post {
    always {
			archiveArtifacts 'build/distributions/*'
			junit 'build/reports/**/*.xml'
		}
	}
}