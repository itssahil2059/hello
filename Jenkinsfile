pipeline {
  agent any

  environment {
    APP_NAME   = 'hello'
    IMAGE_TAG  = "0.0.${env.BUILD_NUMBER}"
    DOCKER_IMG = 'sahilsince2059/hello'
    EC2_HOST   = 'ec2-3-145-131-238.us-east-2.compute.amazonaws.com'

    PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    // DOCKER_HOST intentionally not forced; let Docker Desktop provide it
  }

  tools {
    maven 'Maven3'
    jdk   'JDK21'
  }

  options {
    skipDefaultCheckout(true)
    timestamps()
  }

  parameters {
    booleanParam(name: 'DEPLOY', defaultValue: false, description: 'Build & deploy to EC2 (not required for the quiz)')
  }

  stages {

    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/main']],
          userRemoteConfigs: [[url: 'https://github.com/itssahil2059/hello.git']]
        ])
      }
    }

    stage('Test + Coverage') {
      steps {
        sh 'mvn -q clean verify jacoco:report'

        // Publish JaCoCo to Jenkins UI (Code Coverage API plugin)
        publishCoverage(
          adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')],
          sourceFileResolver: sourceFiles('STORE_LAST_BUILD'),
          failNoReports: true,
          globalThresholds: [
            [thresholdTarget: 'Line',   unhealthyThreshold: '95', unstableThreshold: '95'],
            [thresholdTarget: 'Branch', unhealthyThreshold: '50', unstableThreshold: '50']
          ]
        )

        // Keep the HTML report for screenshots
        archiveArtifacts artifacts: 'target/site/jacoco/**', fingerprint: true
      }
    }

    stage('Build & Push Docker') {
      when { expression { return params.DEPLOY?.toBoolean() } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh '''
            set -e
            docker version
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin

            docker buildx inspect jenx >/dev/null 2>&1 || docker buildx create --use --name jenx
            docker buildx use jenx

            docker buildx build --platform linux/amd64 \
              -t ${DOCKER_IMG}:${IMAGE_TAG} \
              -t ${DOCKER_IMG}:latest \
              --push .
          '''
        }
      }
    }

    stage('Deploy on EC2') {
      when { expression { return params.DEPLOY?.toBoolean() } }
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'ec2-creds', keyFileVariable: 'EC2_KEY', usernameVariable: 'EC2_USER')]) {
          sh """
            set -e
            ssh -o StrictHostKeyChecking=no -i "$EC2_KEY" "$EC2_USER@${EC2_HOST}" "\
              set -e; \
              docker pull ${DOCKER_IMG}:latest; \
              docker rm -f ${APP_NAME} || true; \
              docker run -d --name ${APP_NAME} --restart=always -p 8080:8080 ${DOCKER_IMG}:latest; \
              docker ps --format 'table {{.Names}}\\t{{.Image}}\\t{{.Ports}}' \
            "
          """
        }
      }
    }
  }

  post {
    always {
      echo "Build ${env.BUILD_NUMBER} finished with status: ${currentBuild.currentResult}"
    }
  }
}
