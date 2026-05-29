pipeline {
    agent any    
    environment {
        SERVICE        = 'product'
        AWS_ACCOUNT_ID = "${env.AWS_ACCOUNT_ID}" 
        AWS_REGION     = "${env.AWS_REGION}"           
        ECR_REGISTRY   = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
        CLUSTER_NAME   = "${env.CLUSTER_NAME}"
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -U -DskipTests clean install'
            }
        }
        
        stage('Build & Push to Amazon ECR') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding', 
                    credentialsId: 'aws-cynthia-keys'
                ]]) {
                    sh "aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.ECR_REGISTRY}"
                    
                    sh "docker build -t ${env.ECR_REGISTRY}/${env.SERVICE}:latest -f ../product-service/Dockerfile ."
                    
                    sh "docker push ${env.ECR_REGISTRY}/${env.SERVICE}:latest"
                }
            }
        }
        
        stage('Deploy to Amazon EKS') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding', 
                    credentialsId: 'aws-cynthia-keys'
                ]]) {
                    sh "aws eks update-kubeconfig --name ${env.CLUSTER_NAME} --region ${env.AWS_REGION}"
                    
                    sh "kubectl apply -f ../product-service/k8s/k8s.yaml"
                }
            }
        }
    }
}