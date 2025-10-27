pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "sahilsince2059/hello"
    DOCKER_TAG   = "0.0.${BUILD_NUMBER}"
    EC2_HOST     = "ec2-xx-xxx-xxx-xxx.us-east-2.compute.amazonaws.com" // Replace with your EC2 DNS
    EC2_USER     = "ubuntu"
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build with Maven') {
      steps {
        sh 'mvn -q -B -DskipTests package'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
      }
    }

    stage('Push to DockerHub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
          sh """
            echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
            docker logout
          """
        }
      }
    }

    stage('Deploy on EC2') {
      steps {
        sshagent(credentials: ['ec2-creds']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
              sudo docker rm -f hello || true &&
              sudo docker pull ${DOCKER_IMAGE}:${DOCKER_TAG} &&
              sudo docker run -d --name hello -p 9090:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
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
