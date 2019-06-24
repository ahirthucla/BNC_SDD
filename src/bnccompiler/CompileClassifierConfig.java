package bnccompiler;

import bnccompiler.core.*;

/** Import statements necessary for il2 classes. */
import il2.bridge.*;
import il2.io.Uai;
import il2.model.*;
import il2.model.Table;
import il2.util.*;

/** Import standard Java classes. */
import java.util.*;
import java.lang.*;
import java.io.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CompileClassifierConfig {
  private String id;
  private String root;
  private String filetype;
  private String name;
  private String vars;
  private String[] leaves;
  private String threshold;
  private String input_filepath;
  private String output_filepath;

  //new fields with the intent of not duplicating effort in CompileClassifier constructor
  //this is necessary because a bnc and bnc_compilation_order must be created in order to find out vars
  private BayesianNetworkClassifier bnc;
  private BayesianNetworkClassifierCompilationOrder bnc_compilation_order;

  public String getId () {
   return id;
  }
  public void setId (String id) {
    this.id = id;
  }
  public String getRoot () {
    return root;
  }
  public void setRoot (String root) {
    this.root = root;
  }
  public String getFiletype () {
    return filetype;
  }
  public void setFiletype (String filetype) {
    this.filetype = filetype;
  }
  public String getVars () {
    return vars;
  }
  public void setVars (String vars) {
    this.vars = vars;
  }
  public String getName () {
    return name;
  }
  public void setName (String name) {
    this.name = name;
  }
  public String[] getLeaves () {
    return leaves;
  }
  public void setLeaves (String[] leaves) {
    this.leaves = leaves;
  }
  public String getThreshold () {
    return threshold;
  }
  public void setThreshold (String threshold) {
    this.threshold = threshold;
  }
  public String getInput_filepath () {
    return input_filepath;
  }
  public void setInput_filepath (String input_filepath) {
    this.input_filepath = input_filepath;
  }
  public String getOutput_filepath () {
    return output_filepath;
  }
  public void setOutput_filepath (String output_filepath) {
    this.output_filepath = output_filepath;
  }

  //new getters and setters for our new fields
  @JsonIgnore
  public BayesianNetworkClassifier getBnc(){
    return bnc;
  }
  @JsonIgnore
  public void setBnc(BayesianNetworkClassifier bnc){
    this.bnc = bnc;
  }
  @JsonIgnore
  public BayesianNetworkClassifierCompilationOrder getBncOrder(){
    return bnc_compilation_order;
  }
  @JsonIgnore
  public void setBncOrder(BayesianNetworkClassifierCompilationOrder bnc_compilation_order){
    this.bnc_compilation_order = bnc_compilation_order;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = "+id+", root = "+root+", name = "+name+", vars = "+vars+", leaves = "+leaves+", threshold = "+threshold+", input_filepath = "+input_filepath+", output_filepath = "+output_filepath+"]";
  }

 //new helper function to read a bn. this is the work copied from CompileClassifier constructor
  public BayesianNetwork readBn(String input_filename){
    BayesianNetwork bn = null;

    try {
      switch (getFiletype()){
        case "net":
          bn = IO.readNetwork(input_filename);
          break;
        case "uai":
          bn = Uai.uaiToBayesianNetwork(input_filename);
          break;
        default:
          bn = null;
      }
      if (bn == null){
        throw new Exception("File type is unsupported: " + getFiletype());
      }
    } catch (Exception e){
      System.out.println(e);
    }

    File output_filepath = new File(getOutput_filepath());
    output_filepath.mkdirs();

    return bn;
  }

  //helper function to get the input network_file filename
  @JsonIgnore
  public String getInputFilename(){
    return getInput_filepath()+getName()+"."+getFiletype();
  }


  //initialize bnc for the config file version. used in loadConfig of RunCompiler
  //this is necessary because this is where readbn is called for the config_file version
  //otherwise, the ObjectMapper would write all of the old fields into the file, and the work that was previously in the CompileClassifier constructor would never be run.
  //this MUST be called after the ObjectMapper runs readValue, and can't be placed in the default constructor because the defaultConstructor is run before readValue
  public void initializeBnc(){
    initializeBnc(readBn(getInputFilename()));
  }

  //initialize bnc sets the bnc and bnc_compilation_order fields.
  //it is it's own function because it is used in both the config_file and network_file versions of the constructor
  //it returns a BayesianNetworkClassifierCompilationOrder for use in the network_file version
  public BayesianNetworkClassifierCompilationOrder initializeBnc(BayesianNetwork bn){

    BayesianNetworkClassifier bnc = new BayesianNetworkClassifier(bn, getRoot(), getLeaves(), Double.parseDouble(getThreshold()));

    BayesianNetworkClassifierCompilationOrder bnc_compilation_order = new BayesianNetworkClassifierCompilationOrder(bnc);

    //include bnc and compilation order in config
    setBnc(bnc);
    setBncOrder(bnc_compilation_order);
    return bnc_compilation_order;
  }

  //This is the function that checks if the network is both binary and non-hard.
  //Currently it is only being run for the network_file version
  public boolean validateBinaryHard(BayesianNetwork bn) throws Exception{
    // need to discover root,thresh by parsing the network
    Table[] cpts = bn.cpts();
    IntSet roots = bn.generateGraph().roots();

    //check all the cpts
    for (int index = 0; index < cpts.length; index++){
      Table cpt = cpts[index];

      double[] values = cpt.values();

      //check every value for binary/hardness
      try{
        //this relies on consistent ordering between generateGraph and the cpts.
        //If the cpt in question is both a root and contains more than 2 entries, it doesn't represent a binary tree
        if (values.length > 2 && roots.contains(index)){
          throw new Exception("Non-binary network, found in cpt: "+cpt.varString());
        }
        //if any of the values are 1 or 0, the network isn't hard
        for (double value : values){
          if (value == 1.0 || value == 0.0){
            throw new Exception("Non-soft network, found in cpt: "+cpt.varString());
          }
        }
      } catch (Exception e){
        throw e;
      }
    }
    return true;
  }


  //This returns a list of CompileClassifierConfig instantiations, one for each root in the root list derived from the BayesianNetwork
  //Only used in ConfigGenerator so far
  public List<CompileClassifierConfig> allRootConfigs(){
    System.out.println("Root not given, generating config files for every root");

    BayesianNetwork bn = readBn(getInputFilename());

    //get root list
    int[] roots = bn.generateGraph().roots().toArray();
    List<CompileClassifierConfig> configlist = new ArrayList<CompileClassifierConfig>();
    //iterate over root list
    for (int root: roots){
      CompileClassifierConfig conf = null;
      try{
        //create new config instantiation for that particular root
        //currently sets id to root index
        conf = new CompileClassifierConfig(getInputFilename(),Integer.toString(root), bn.domain().name(root), null);
      }catch (Exception e){
        //if that root causes some sort of error, don't generate that config
        System.out.println("exception caught in config generation");
        conf = null;
      }
      //only add each valid config
      if(conf != null){
        configlist.add(conf);
      }
    }
    return configlist;
  }


  //the constructor or the config_file version
  //this is maintained because the current code both requires a zero-arg constructor
  //RunCompiler sets all non (bnc, bnc_compile_order) variables with ObjectMapper.readValue, so we need to set those new fields after running this constructor
  //It currently runs before readValue fills in the values.
  public CompileClassifierConfig(){}


  //the constructor for the network_file version
  //sets all the fields based on the network_file instead of having them pasted in by the ObjectMapper, from the config file
  public CompileClassifierConfig(String input_filename, String id, String root, String threshold)throws Exception{

    //Set id, name, filetype, input filepath, output filepath from input filename
    int slashindex = input_filename.lastIndexOf("/");
    int dotindex = input_filename.lastIndexOf(".");
    if(id != null){
      setId(id);
    }else{
      setId("1");//arbitrary
    }
    setName(input_filename.substring(slashindex+1, dotindex));
    setFiletype(input_filename.substring(dotindex+1));
    setInput_filepath(input_filename.substring(0, slashindex+1));
    setOutput_filepath("output/");

    //leaves assumed []
    setLeaves(new String[0]);

    // need to discover root,thresh by parsing the network
    BayesianNetwork bn = readBn(input_filename);

    //check for binaryness/hardness
    validateBinaryHard(bn);


    Table[] cpts = bn.cpts();
    IntSet roots = bn.generateGraph().roots();
    Domain domain = bn.domain();

    //setting the root
    if(root == null){//if not passed in as arg
      root = domain.name(roots.get(0));//select the first root
    }else{//if passed in as arg
      try{
        domain.index(root);//check if root is in domain
        //domain is a set based data structure why doesn't it have contains
      }catch (NullPointerException e){
        throw new NullPointerException("root not in the root set: "+root);
      }
    }
    setRoot(root);

    //setting the threshold
    if(threshold == null){//if not passed in as arg
      int rootindex = domain.index(root);
      //select the second entry of the root's cpt (the "no" values) as the threshold
      threshold = Double.toString(cpts[rootindex].values()[1]);
    }else{//if passed in as arg
      try{
        double thresh = Double.parseDouble(threshold);//check that threshold is valid double
        if  (0.0 >= thresh || thresh >= 1.0){//check that threshold is valid percentage
          throw new NumberFormatException();
        }
      }catch (NumberFormatException e){
        throw new NumberFormatException("non-double or non-percentage threshold: "+threshold);
      }
    }
    setThreshold(threshold);

    //sets bnc, bnc_compiler_order, return order for setting vars.
    BayesianNetworkClassifierCompilationOrder bnc_compilation_order = initializeBnc(bn);

    //set variable count based on compilation order
    setVars(Integer.toString(bnc_compilation_order.getFeature_order().length));

  }
}
