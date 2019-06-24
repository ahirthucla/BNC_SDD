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

public class ConfigGenerator {

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
    if (args.length != 1){
      System.out.println("Must have 1 argument (network_file)");
      return;
    }
    ConfigGenerator T = new ConfigGenerator();
    CompileClassifierConfig config = null;
    System.out.println("config generator");
    String input_filename = args[0];
    config = new CompileClassifierConfig(input_filename,null,null,null);
    //config automatically takes the first root in the root list
    //The below saves config files for all the roots, but doesn't operate on them. 
    List<CompileClassifierConfig> configs = config.allRootConfigs();
    for (CompileClassifierConfig conf :configs){
      System.out.println(conf);
      saveConfig(conf);
    }
  }
}

