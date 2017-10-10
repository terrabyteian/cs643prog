My Code

Description:

My code chains multiple map-reduce jobs together to come up with the results to solve problems 2a and 2b. 

Job1 does a basic wordcount on all the inputs to find each occurence of one of the 4 words.
Job2 condenses the results of job1 to key-val pairs of <State>-<Rankings>, where rankings are in the following format:

...
Oklahoma	1:education 2:sports 3:politics 4:agriculture 
Oregon	1:education 2:sports 3:agriculture 4:politics 
Pennsylvania	1:agriculture 2:education 3:sports 4:politics 
Rhode_Island	1:politics 2:sports 3:agriculture 4:education 
...

If a state has the same count of multiple words (i.e. 2 education and 2 sports), it will take the first value it comes across and set it as the more dominant. This helps the code maintain a 1-2-3-4 ranking system. 


Job3 picks out the first word (being the most dominant) and sets it as the key. Then the basic wordcount reducer calculates how many times each word occured. 

Job4 takes the results of job2, sets the value to the key, and appends all the states to the value in the reducer. 



How to run:

1. Pull down my code

cd ~
wget https://github.com/terrabyteian/cs643prog/archive/master.zip
unzip master.zip
mv cs643proj-master cs643proj

2. Pull down Wikipedia pages to ~/states using the script I wrote

mkdir states
cs643proj/pull-states.sh states

3. Make input/output directories in hdfs and move states to it

cd hadoop-2.7.4
bin/hdfs dfs -mkdir /input/
bin/hdfs dfs -mkdir /input/states
bin/hdfs dfs -mkdir /output/
bin/hdfs dfs -put ~/states/* /input/states/

4. Run code

bin/hadoop jar ~/cs643proj/sw.jar StateWords /input/states /output/
bin/hdfs dfs -cat /output/job1/*
bin/hdfs dfs -cat /output/job2/*
bin/hdfs dfs -cat /output/job3/*
bin/hdfs dfs -cat /output/job4/*



Results from my CentOS VM:

[ian@localhost hadoop-2.7.4]$ bin/hdfs dfs -cat /user/ian/output/states/job2/*
Alabama	1:education 2:sports 3:politics 4:agriculture 
Alaska	1:education 2:agriculture 3:politics 4:sports 
Arizona	1:education 2:politics 3:sports 4:agriculture 
Arkansas	1:education 2:agriculture 3:politics 4:sports 
California	1:education 2:sports 3:agriculture 4:politics 
Colorado	1:sports 2:agriculture 3:education 4:politics 
Connecticut	1:sports 2:education 3:politics 4:agriculture 
Delaware	1:education 2:agriculture 3:sports 4:politics 
Florida	1:education 2:sports 3:agriculture 4:politics 
Georgia	1:politics 2:agriculture 3:education 4:sports 
Hawaii	1:education 2:politics 3:agriculture 4:sports 
Idaho	1:politics 2:education 3:sports 4:agriculture 
Illinois	1:sports 2:education 3:politics 4:agriculture 
Indiana	1:sports 2:agriculture 3:politics 4:education 
Iowa	1:agriculture 2:education 3:sports 4:politics 
Kansas	1:sports 2:education 3:politics 4:agriculture 
Kentucky	1:education 2:agriculture 3:politics 4:sports 
Louisiana	1:agriculture 2:education 3:sports 4:politics 
Maine	1:politics 2:sports 3:agriculture 4:education 
Maryland	1:education 2:agriculture 3:sports 4:politics 
Massachusetts	1:education 2:sports 3:politics 4:agriculture 
Michigan	1:education 2:sports 3:politics 4:agriculture 
Minnesota	1:sports 2:education 3:agriculture 4:politics 
Mississippi	1:education 2:agriculture 3:politics 4:sports 
Missouri	1:education 2:agriculture 3:sports 4:politics 
Montana	1:education 2:sports 3:agriculture 4:politics 
Nebraska	1:agriculture 2:education 3:sports 4:politics 
Nevada	1:sports 2:agriculture 3:education 4:politics 
New_Hampshire	1:sports 2:education 3:agriculture 4:politics 
New_Jersey	1:education 2:sports 3:agriculture 4:politics 
New_Mexico	1:education 2:sports 3:agriculture 4:politics 
New_York	1:agriculture 2:education 3:politics 4:sports 
North_Carolina	1:sports 2:education 3:politics 4:agriculture 
North_Dakota	1:agriculture 2:education 3:politics 4:sports 
Ohio	1:sports 2:education 3:agriculture 4:politics 
Oklahoma	1:education 2:sports 3:politics 4:agriculture 
Oregon	1:education 2:sports 3:agriculture 4:politics 
Pennsylvania	1:agriculture 2:education 3:sports 4:politics 
Rhode_Island	1:politics 2:sports 3:agriculture 4:education 
South_Carolina	1:education 2:agriculture 3:politics 4:sports 
South_Dakota	1:education 2:agriculture 3:sports 4:politics 
Tennessee	1:education 2:agriculture 3:politics 4:sports 
Texas	1:education 2:sports 3:politics 4:agriculture 
Utah	1:sports 2:politics 3:agriculture 4:education 
Vermont	1:sports 2:education 3:politics 4:agriculture 
Virginia	1:education 2:agriculture 3:sports 4:politics 
Washington	1:agriculture 2:education 3:politics 4:sports 
West_Virginia	1:education 2:politics 3:sports 4:agriculture 
Wisconsin	1:politics 2:sports 3:agriculture 4:education 
Wyoming	1:sports 2:agriculture 3:politics 4:education 

[ian@localhost hadoop-2.7.4]$ bin/hdfs dfs -cat /user/ian/output/states/job3/*
agriculture	7
education	25
politics	5
sports	13

[ian@localhost hadoop-2.7.4]$ bin/hdfs dfs -cat /user/ian/output/states/job4/*
1:agriculture 2:education 3:politics 4:sports 	 North_Dakota New_York Washington
1:agriculture 2:education 3:sports 4:politics 	 Nebraska Iowa Louisiana Pennsylvania
1:education 2:agriculture 3:politics 4:sports 	 Mississippi Arkansas Alaska Tennessee South_Carolina Kentucky
1:education 2:agriculture 3:sports 4:politics 	 Maryland Virginia Missouri Delaware South_Dakota
1:education 2:politics 3:agriculture 4:sports 	 Hawaii
1:education 2:politics 3:sports 4:agriculture 	 West_Virginia Arizona
1:education 2:sports 3:agriculture 4:politics 	 Oregon Florida New_Mexico New_Jersey Montana California
1:education 2:sports 3:politics 4:agriculture 	 Alabama Texas Oklahoma Michigan Massachusetts
1:politics 2:agriculture 3:education 4:sports 	 Georgia
1:politics 2:education 3:sports 4:agriculture 	 Idaho
1:politics 2:sports 3:agriculture 4:education 	 Maine Rhode_Island Wisconsin
1:sports 2:agriculture 3:education 4:politics 	 Nevada Colorado
1:sports 2:agriculture 3:politics 4:education 	 Wyoming Indiana
1:sports 2:education 3:agriculture 4:politics 	 New_Hampshire Ohio Minnesota
1:sports 2:education 3:politics 4:agriculture 	 Connecticut Illinois Kansas Vermont North_Carolina
1:sports 2:politics 3:agriculture 4:education 	 Utah

