# NJIT CS 643 F17 Project by Ian Hall

Please note that Project code is derived from the MapReduce WordCount example found here: https://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html 

And note that the cluster it built loosely based on the steps listed here: https://www.tutorialspoint.com/hadoop/hadoop_multi_node_cluster.htm 

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

## Configure Cluster

Using the above custom Hadoop AMI, perform following steps:

1. Pick a master IP and slave IPs. Add following lines to /etc/hosts on each node (subsitute IP addresses with public IPs of your cluster)
```
172.31.11.255 hadoop-master
172.31.5.91 hadoop-slave1
172.31.1.126 hadoop-slave2
172.31.3.97 hadoop-slave3
```
2. Configure passwordless login on each node, using the .pem file provided by AWS. This is assuming each node uses the same key configured by AWS. 
* modify ~/.ssh/key.pem and add contents of your .pem
* modify ~/.ssh/config, add "IdentityFile ~/.ssh/key.pem"
* Change ownership
```bash
chmod 600 ~/.ssh/config
chmod 600 ~/.ssh/key.pem
```
3. Change content of following files on hadoop-master:
```bash
cd ~/hadoop-2.7.4
```
* etc/hadoop/core-site.xml
```xml
<configuration>
	<property>
		<name>fs.defaultFS</name>
		<value>hdfs://hadoop-master:9000</value>
	</property>
	<property>
		<name>dfs.permissions</name>
		<value>false</value>
	</property>
</configuration>
```
* etc/hadoop/yarn-site.xml
```xml
<configuration>

<!-- Site specific YARN configuration properties -->
   <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property>
  <property>
    <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
    <value>org.apache.hadoop.mapred.ShuffleHandler</value>
  </property>
  <property>
    <name>yarn.resourcemanager.hostname</name>
    <value>hadoop-master</value>
  </property>
</configuration>
```
* etc/hadoop/hdfs-site.xml
```xml
<configuration>
	<property>
		<name>dfs.replication</name>
		<value>3</value>
	</property>
</configuration>
```
* etc/hadoop/mapred-site.xml
```xml
<configuration>
	<property>
		<name>mapreduce.jobtracker.address</name>
		<value>hadoop-master:54311</value>
	</property>
	<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
	</property>
</configuration>
```
* etc/hadoop/masters
```
hadoop-master
```
* etc/hadoop/slaves
```
hadoop-slave1
hadoop-slave2
hadoop-slave3
```
* Distribute xml files to other nodes
```bash
scp etc/hadoop/*.xml hadoop-slave1:~/hadoop-2.7.4/etc/hadoop/
scp etc/hadoop/*.xml hadoop-slave2:~/hadoop-2.7.4/etc/hadoop/
scp etc/hadoop/*.xml hadoop-slave3:~/hadoop-2.7.4/etc/hadoop/
```
4. Format namenode on master
```bash
bin/hadoop namenode -format
```
5. Start Hadoop Services
```bash
sbin/start-dfs.sh
sbin/start-yarn.sh
```

Here is some sample output of start-dfs.sh for my cluster, to prove I got it working in my AWS EC2 environment:
```bash
[ec2-user@ip-172-31-11-255 hadoop-2.7.4]$ cat /etc/hosts
127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
::1         localhost6 localhost6.localdomain6

172.31.11.255 hadoop-master
172.31.5.91 hadoop-slave1
172.31.1.126 hadoop-slave2
172.31.3.97 hadoop-slave3
[ec2-user@ip-172-31-11-255 hadoop-2.7.4]$ sbin/start-dfs.sh
Starting namenodes on [hadoop-master]
hadoop-master: starting namenode, logging to /home/ec2-user/hadoop-2.7.4/logs/hadoop-ec2-user-namenode-ip-172-31-11-255.out
hadoop-slave2: starting datanode, logging to /home/ec2-user/hadoop-2.7.4/logs/hadoop-ec2-user-datanode-ip-172-31-1-126.out
hadoop-slave3: starting datanode, logging to /home/ec2-user/hadoop-2.7.4/logs/hadoop-ec2-user-datanode-ip-172-31-3-97.out
hadoop-slave1: starting datanode, logging to /home/ec2-user/hadoop-2.7.4/logs/hadoop-ec2-user-datanode-ip-172-31-5-91.out
Starting secondary namenodes [0.0.0.0]
0.0.0.0: starting secondarynamenode, logging to /home/ec2-user/hadoop-2.7.4/logs/hadoop-ec2-user-secondarynamenode-ip-172-31-11-255.out
[ec2-user@ip-172-31-11-255 hadoop-2.7.4]$
```

## Run My MapReduce Project

1. Pull down my code
```bash
cd ~
wget https://github.com/terrabyteian/cs643prog/archive/master.zip
unzip master.zip
mv cs643proj-master cs643proj
```
2. Pull down Wikipedia pages to ~/states using the script I wrote
```bash
mkdir states
cs643proj/pull-states.sh states
```
3. Make input/output directories in hdfs and move states to it
```bash
cd hadoop-2.7.4
bin/hdfs dfs -mkdir /input/
bin/hdfs dfs -mkdir /input/states
bin/hdfs dfs -mkdir /output/
bin/hdfs dfs -put ~/states/* /input/states/
```
4. Run code
```bash
bin/hadoop jar ~/cs643proj/sw.jar StateWords /input/states /output/
bin/hdfs dfs -cat /output/job1/*
bin/hdfs dfs -cat /output/job2/*
bin/hdfs dfs -cat /output/job3/*
bin/hdfs dfs -cat /output/job4/*
```
