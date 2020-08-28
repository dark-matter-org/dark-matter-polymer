#!/usr/bin/groovy

@Library('ci-pipeline-library') _

def includeRunConfigTags = 'default'
def excludeRunConfigTags = ''

pipeline {
  agent {
    label 'lsc-ubuntu1404-ci-robot-latest'
  }

  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr:'5'))
    timeout(time: 120, unit: 'MINUTES')
    timestamps()
  }

  stages {
    stage('PREPARE') {
      steps {
        prepareBuild()
      }
    }

    stage('BUILD DEPENDENCIES') {
      when {
        not {
          expression {
            return isPublishableBranch()
          }
        }
      }

      steps {
        script {
          if (env.CHANGE_BRANCH) {
            env.CLONE_BRANCH = env.CHANGE_BRANCH
          } else {
            env.CLONE_BRANCH = env.BRANCH_NAME
          }
        }
        buildExistingBranches  branch: env.CLONE_BRANCH
      }
    }

    stage('BUILD') {
      steps {
        buildMaven()
      }
    }

    stage('PUBLISH') {
      when {
        expression {
          return isPublishableBranch()
        }
      }
      steps {
        deployMaven()
      }
    }
  }

  post {
    failure {
      notifyEmail emailBody: 'Build failed. Please check the logs.'
    }
    always {
      finishBuild()
    }
  }
}