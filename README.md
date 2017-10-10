# NJIT CS 643 F17 Project by Ian Hall

## AMI Build Steps

As user ec2-user, performed the following steps:

1. Install java-1.8 openjdk: `
```bash
sudo yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel
```
2. Add following lines to /etc/profile
```bash
# Hadoop
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.141-1.b16.32.amzn1.x86_64/
export HADOOP_CLASSPATH=$JAVA_HOME/lib/tools.jar
export PATH=$JAVA_HOME/bin/:$PATH
```
3. Install latest stable hadoop to ec2-user home dir (2.7.4)
```bash
cd ~
wget http://apache.claz.org/hadoop/common/hadoop-2.7.4/hadoop-2.7.4.tar.gz
tar -zxvf hadoop-2.7.4.tar.gz
```

## My AMI

My AMI:

AMI ID: ami-6bde1111

AMI Name: Hadoop-AMI

Owner: 567025740324

Source: 567025740324/Hadoop-AMI

## Name Node Configuration

Using the above custom Hadoop AMI, perform following steps:

