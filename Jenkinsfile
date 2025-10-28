pipeline {
  agent any

  tools {
    jdk   'JDK17'       // must match Manage Jenkins → Tools
    maven 'Maven3'      // must match Manage Jenkins → Tools
  }

  environment {
    DOCKER_IMAGE = "sahilsince2059/hello"
    DOCKER_TAG   = "0.0.${BUILD_NUMBER}"
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
          echo "===== JAVA ====="
          which java || true
          java -version || true

          echo "===== MAVEN ====="
          which mvn || true
          mvn -v || true

          echo "===== DOCKER ====="
          which docker || true
          docker -v || true
        '''
      }
    }

    stage('Build with Maven') {
      steps {
        sh 'mvn -q -B -DskipTests package'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
      }
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
            docker logout
          '''
        }
      }
    }

    stage('Deploy on EC2') {
      steps {
        sshagent(credentials: ['ec2-creds']) {
          sh '''
            ssh -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST '
              # Ensure Docker exists on EC2
              if ! command -v docker >/dev/null 2>&1; then
                sudo apt-get update -y &&
                sudo apt-get install -y docker.io &&
                sudo systemctl enable --now docker
              fi

              sudo docker rm -f hello || true
              sudo docker pull '$DOCKER_IMAGE':'$DOCKER_TAG'
              sudo docker run -d --name hello -p 9090:8080 '$DOCKER_IMAGE':'$DOCKER_TAG'
            '
          '''
        }
      }
    }
  }

  post {
    success {
      echo "Deployed: http://${env.EC2_HOST}:9090/"
    }
  }
}
