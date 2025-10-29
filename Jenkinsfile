pipeline {
  agent any

  environment {
    APP_NAME   = 'hello'
    IMAGE_TAG  = "0.0.${env.BUILD_NUMBER}"
    REGISTRY   = 'docker.io'
    DOCKER_IMG = "sahilsince2059/hello"
    EC2_HOST   = "ec2-3-145-131-238.us-east-2.compute.amazonaws.com"

    // Add these two lines:
    PATH       = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    DOCKER_HOST = "unix:///Users/sahilbhusal/.docker/run/docker.sock"
  }

  tools {
    maven 'Maven3'
    jdk   'JDK21'
  }

  stages {
    stage('Checkout') { steps { git branch: 'main', url: 'https://github.com/itssahil2059/hello.git' } }

    stage('Build with Maven') { steps { sh 'mvn -B -q -DskipTests package' } }

    stage('Build & Push Docker') {
      steps {
        sh 'docker version'                 // <— sanity check; leave it for now
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh '''
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
            docker buildx create --use --name jenx || docker buildx use jenx
            docker buildx build --platform linux/amd64 \
              -t ${DOCKER_IMG}:${IMAGE_TAG} -t ${DOCKER_IMG}:latest --push .
          '''
        }
      }
    }

    stage('Deploy on EC2') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'ec2-creds', keyFileVariable: 'EC2_KEY', usernameVariable: 'EC2_USER')]) {
          sh '''
            ssh -o StrictHostKeyChecking=no -i "$EC2_KEY" "$EC2_USER"@"${EC2_HOST}" '
              set -e
              docker pull ${DOCKER_IMG}:latest
              docker rm -f ${APP_NAME} || true
              docker run -d --name ${APP_NAME} -p 8080:8080 ${DOCKER_IMG}:latest
            '
          '''
        }
      }
    }
  }

  post { always { echo "Build ${env.BUILD_NUMBER} finished with status: ${currentBuild.currentResult}" } }
}
