pipeline {
  agent any

  /* Keep env minimal; no hard-coded DOCKER_HOST */
  environment {
    APP_NAME   = 'hello'
    IMAGE_TAG  = "0.0.${env.BUILD_NUMBER}"
    DOCKER_IMG = 'sahilsince2059/hello'
    EC2_HOST   = 'ec2-3-145-131-238.us-east-2.compute.amazonaws.com'
  }

  tools {
    maven 'Maven3'
    jdk   'JDK21'
  }

  /* Only safe, supported options */
  options {
    skipDefaultCheckout(true)
    timestamps()
  }

  /* Toggle deploy stages from the UI */
  parameters {
    booleanParam(name: 'DEPLOY', defaultValue: false, description: 'Build & deploy Docker image to EC2 (not needed for the quiz)')
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

        // Publish JaCoCo to Jenkins (no Jenkins-side thresholds)
        publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')],
                        sourceFileResolver: sourceFiles('STORE_LAST_BUILD'),
                        failNoReports: true

        // Keep the HTML report as a build artifact (for screenshots)
        archiveArtifacts artifacts: 'target/site/jacoco/**', fingerprint: true
      }
    }

    stage('Build & Push Docker') {
      when { expression { return params.DEPLOY } }
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
      when { expression { return params.DEPLOY } }
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'ec2-creds', keyFileVariable: 'EC2_KEY', usernameVariable: 'EC2_USER')]) {
          sh """
            set -e
            ssh -o StrictHostKeyChecking=no -i "$EC2_KEY" "$EC2_USER@${EC2_HOST}" "\
              set -e; \
              echo 'Pulling image: ${DOCKER_IMG}:latest'; \
              docker pull ${DOCKER_IMG}:latest; \
              docker rm -f ${APP_NAME} || true; \
              docker run -d --name ${APP_NAME} --restart=always -p 8080:8080 ${DOCKER_IMG}:latest; \
              docker ps --format 'table {{.Names}}\\t{{.Image}}\\t{{.Ports}}' \
            "
            echo
            echo "=============================================================="
            echo "✅ Deployed Successfully — http://${EC2_HOST}:8080/"
            echo "=============================================================="
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
