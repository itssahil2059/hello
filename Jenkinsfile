pipeline {
  agent any

  tools {
    jdk   'JDK17'
    maven 'Maven3'
  }

  environment {
    // make docker & mvn visible to Jenkins
    PATH = "/usr/local/bin:/opt/homebrew/bin:/Applications/Docker.app/Contents/Resources/bin:${env.PATH}"

    DOCKER_IMAGE = "sahilsince2059/hello"
    DOCKER_TAG   = "0.0.${BUILD_NUMBER}"

    // your EC2 details
    EC2_HOST     = "ec2-3-145-131-238.us-east-2.compute.amazonaws.com"
    EC2_USER     = "besahil"
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Verify tools') {
      steps {
        sh '''
          echo "whoami: $(whoami)"
          which mvn || true
          mvn -v || true
          which docker || true
          docker version || true
        '''
      }
    }

    stage('Build with Maven') {
      steps { sh 'mvn -q -B -DskipTests package' }
    }

    stage('Build Docker Image') {
      steps { sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .' }
    }

    stage('Push to DockerHub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                          usernameVariable: 'DH_USER',
                                          passwordVariable: 'DH_PASS')]) {
          sh '''
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
            docker push $DOCKER_IMAGE:$DOCKER_TAG
            docker tag  $DOCKER_IMAGE:$DOCKER_TAG $DOCKER_IMAGE:latest
            docker push $DOCKER_IMAGE:latest
          '''
        }
      }
    }

    stage('Deploy on EC2') {
      steps {
        sshagent(['ec2-creds']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
              docker rm -f hello || true &&
              docker pull ${DOCKER_IMAGE}:${DOCKER_TAG} &&
              docker run -d --name hello -p 9090:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
            '
          """
        }
      }
    }
  }

  post {
    success {
      echo "Deployed Successfully → http://${env.EC2_HOST}:9090/"
    }
  }
}
