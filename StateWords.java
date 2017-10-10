import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

// Additional imports
import java.util.Arrays;
import java.util.Comparator;

public class StateWords {

  
  // Words to check for
  public static String[] words = {"education","politics","sports","agriculture"};



  // Maps state-word keys to a 0 or 1
  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable zero = new IntWritable(0);
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString().toLowerCase());
      
      // Get filename to use as state value
      String state = ((FileSplit) context.getInputSplit()).getPath().getName();
	
      while (itr.hasMoreTokens()) {
        String wordStr = itr.nextToken();

	// If word matches one of our words, map it
	for (String topic : words){
	  word.set(state+"-"+topic);
	  if (topic.equals(wordStr)) {
	    context.write(word,one);
	  } else {
	    context.write(word,zero);
          }
	}
      }
    }
  }


  // Sums up total word count for each state
  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      
      // Sum up State-word pairs
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }
  

  // creates state - word+count pairs
  public static class StateWCMapper
       extends Mapper<Object, Text, Text, Text>{

    private Text state = new Text();
    private Text filecount = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      

      // Extract state,word, and count from each line
      String stateword = itr.nextToken();
      String count = itr.nextToken();

      // Send state as key and file-count as value
      String[] pair = stateword.split("-");
      state.set(pair[0]);
      filecount.set(pair[1]+'-'+count);
      context.write(state,filecount);
    }
  }
  

  // Ranks words by state
  public static class RankReducer
       extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      
      // Collect wordcounts into array
      int idx = 0;
      String[] wordcounts = new String[words.length];
      for (Text val : values) {
        wordcounts[idx] = val.toString();
        idx++;
      }

      // Sort wordcounts by rank
      Arrays.sort(wordcounts, new Comparator<String>() {
        @Override
        public int compare(String str1, String str2) {
          String[] split1 = str1.split("-");
          String[] split2 = str2.split("-");
          return Integer.valueOf(split2[1]).compareTo(Integer.valueOf(split1[1]));
        }
      });
      

      // Generate resulting key-pair set
      String res = "";
      idx = 0;
      for (String wordcount : wordcounts){
        String[] split = wordcount.split("-");
        res += Integer.toString(idx+1)+":"+split[0]+" ";
	idx += 1;
      }
      
      Text result = new Text();
      result.set(res);
      context.write(key, result);
    }
  }
  
  // Create key-value pairs for each dominant word occurence
  public static class DominantWordMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      
      // Extract dominant word and spit it out
      String line = value.toString();
      String[] s1 = line.split(" ");
      String[] s2 = s1[0].split(":");
      word.set(s2[1]);
      System.out.println("Word: "+s2[1]);
      context.write(word,one);
	 
    }
  }
  
  // Swaps value to key and key to value
  public static class KVSwapMapper
       extends Mapper<Object, Text, Text, Text>{

    private Text newKey = new Text();
    private Text newValue = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      
      // Set key to old value, and value to old key
      String line = value.toString();
      String[] split = line.split("\\s+",2);
      newKey.set(split[1]);
      newValue.set(split[0]);
      context.write(newKey,newValue);
	 
    }
  }
  

  // Maps all values to one line per unique key 
  public static class SimpleTextValueReducer
       extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      
      // Create resultant line for each unique key
      String res = "";
      for (Text val : values) {
        res = res + " " + val.toString();
      }

      Text result = new Text();
      result.set(res);

      context.write(key, result);
    }
  }
  

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    
    String input=args[0];
    String j1output=args[1]+"/job1";
    String j2output=args[1]+"/job2";
    String j3output=args[1]+"/job3";
    String j4output=args[1]+"/job4";

    // Job 1 - Get <State>-<word> to <count> pairs
    Job job1 = Job.getInstance(conf, "Word Count");
    job1.setJarByClass(StateWords.class);
    job1.setMapperClass(TokenizerMapper.class);
    job1.setCombinerClass(IntSumReducer.class);
    job1.setReducerClass(IntSumReducer.class);
    job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job1, new Path(input));
    FileOutputFormat.setOutputPath(job1, new Path(j1output));
    if (!job1.waitForCompletion(true)) {
      System.exit(1);
    }

    // Job 2 - Get <State> to <Rankings> pairs
    Job job2 = Job.getInstance(conf, "State Rankings by Word");
    job2.setJarByClass(StateWords.class);
    job2.setMapperClass(StateWCMapper.class);
    job2.setReducerClass(RankReducer.class);
    job2.setOutputKeyClass(Text.class);
    job2.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job2, new Path(j1output));
    FileOutputFormat.setOutputPath(job2, new Path(j2output));
    if (!job2.waitForCompletion(true)) {
      System.exit(1);
    }

    // Job 3 - Get # of States where each word is dominant, using exising IntSumReducer
    Job job3 = Job.getInstance(conf, "State Count for Dominant Words");
    job3.setJarByClass(StateWords.class);
    job3.setMapperClass(DominantWordMapper.class);
    job3.setCombinerClass(IntSumReducer.class);
    job3.setReducerClass(IntSumReducer.class);
    job3.setOutputKeyClass(Text.class);
    job3.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job3, new Path(j2output));
    FileOutputFormat.setOutputPath(job3, new Path(j3output));
    if (!job3.waitForCompletion(true)) {
      System.exit(1);
    }
    
    // Job 4 - Get all states that have same ranking pattern
    Job job4 = Job.getInstance(conf, "Get all states for each ranking pattern");
    job4.setJarByClass(StateWords.class);
    job4.setMapperClass(KVSwapMapper.class);
    job4.setReducerClass(SimpleTextValueReducer.class);
    job4.setOutputKeyClass(Text.class);
    job4.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job4, new Path(j2output));
    FileOutputFormat.setOutputPath(job4, new Path(j4output));
    if (!job4.waitForCompletion(true)) {
      System.exit(1);
    }
  }
}
