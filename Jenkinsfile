pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        withGradle() {
          sh './gradlew assemble'
        }

      }
    }

    stage('archive') {
      steps {
        archiveArtifacts 'build/distributions/*'
      }
    }

  }
}