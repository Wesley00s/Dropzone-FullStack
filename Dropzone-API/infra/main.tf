terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

variable "aws_access_key" {
  type    = string
  default = "test"
}

variable "aws_secret_key" {
  type    = string
  default = "test"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "aws_endpoint" {
  type    = string
  default = "http://localhost:4566"
}

provider "aws" {
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
  region     = var.aws_region

  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
  s3_use_path_style           = true

  endpoints {
    s3             = var.aws_endpoint
    sts            = var.aws_endpoint
    iam            = var.aws_endpoint
    cloudwatch     = var.aws_endpoint
    apigateway     = var.aws_endpoint
    route53        = var.aws_endpoint
    route53resolver = var.aws_endpoint
    dynamodb       = var.aws_endpoint
    ec2            = var.aws_endpoint
    lambda         = var.aws_endpoint
    sns            = var.aws_endpoint
    sqs            = var.aws_endpoint
  }
}

resource "aws_s3_bucket" "dropzone_bucket" {
  bucket = "dropzone-files"
}

resource "aws_s3_bucket_cors_configuration" "cors_rules" {
  bucket = aws_s3_bucket.dropzone_bucket.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "HEAD", "DELETE"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}