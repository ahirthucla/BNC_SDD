import bnccompiler.*;

/** Import standard Java classes. */
import java.util.*;
import java.lang.*;
import java.io.*;

import java.nio.charset.Charset;
import java.nio.file.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RunCompiler
{
  private static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private static CompileClassifierConfig loadConfig(String config_file) {
    CompileClassifierConfig config = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = readFile(config_file, Charset.forName("UTF-8"));
      config = mapper.readValue(jsonString, CompileClassifierConfig.class);
      //initialize the fields (bnc, bnc_compilation_order) that aren't stored in the config json
      config.initializeBnc();
    } catch (IOException e) {
      System.out.println(e);
    } catch (Exception e) {
      System.out.println(e);
    }
    return config;
  }

  private static void saveConfig(CompileClassifierConfig config){
    try{
      ObjectMapper mapper = new ObjectMapper();
      String config_filename = config.getOutput_filepath()+config.getName()+"_"+config.getId()+".json";
      mapper.writeValue(new File(config_filename), config);
    } catch (IOException e) {
      System.out.println(e);
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static void main(String[] args)throws Exception{
    if (!(args.length == 1 || args.length == 4)) {
      System.out.println("Must have either 1 argument (config_file) or four arguments (network file, id, root, threshold), in that order");
      return;
    }
    RunCompiler T = new RunCompiler();

    CompileClassifierConfig config = null;
    if (args.length == 1){
      //this is the old functionality of the previous main function
      if ((args[0].substring(args[0].lastIndexOf(".")+1)).equals("json")){
        System.out.println("config version");
        String config_file = args[0];
        config = loadConfig(config_file);
      }else {
        //case 1 for the network_file version (1 argument given)
        System.out.println("net version");
        String input_filename = args[0];
        //config automatically takes the first root in the root list
        config = new CompileClassifierConfig(input_filename,null,null,null);
      }
    } else{
      //case 2 for the network_file version (4 arguments given)
      //argparsing is not rigorous.. yet..
      System.out.println("net version");
      String input_filename = args[0];
      String id = args[1];
      String root = args[2];
      String threshold = args[3];
      config = new CompileClassifierConfig(input_filename, id, root, threshold);
    }
    //save the config generated
    saveConfig(config);

    //back to old code
    CompileClassifier compile_job = new CompileClassifier(config);
    compile_job.run(true);
  }
}

