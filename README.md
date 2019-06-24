# BNC_SDD

Converts a Bayesian Network Classifier (BNC) into a Sentential Decision Diagram (SDD) by first compiling an Ordered Decision Diagram (ODD) and then converting the ODD into a SDD.
The algorithm is described in the following paper:

```
Compiling Bayesian Networks into Decision Graphs
In Proceedings of the Thirty-Third AAAI Conference on Artificial Intelligence (AAAI), 2019
Andy Shih, Arthur Choi, Adnan Darwiche
```

### Input Format

The input BNC is specified using "config.json". It should have a JSON object with the following fields:

- "id": some number to help you distinguish the compilation task
- "name": name of the .net or the .uai Bayesian Network file
- "filetype": filetype of the Bayesian Network file (either ".net" or ".uai")
- "vars": number of feature variables
- "root": class node of the Bayesian Network Classifier
- "leaves": a list of the feature variables. If empty, defaults to all leaves of the Bayesian Network. If not empty, length should match "vars"
- "threshold": threshold of the Bayesian Network Classifier
- "input_filepath": filepath of the Bayesian Network file
- "output_filepath": filepath to write the output ODD and SDD


The underlying Bayesian Network of the BNC is specified as a .net file or a .uai file.
The file that will be looked up is ```config["input_filepath"] + config["name"] + "." + config["filetype"]```.
For example, if the filepath of the network file is at ```"networks/binarynetworks/admission.net"```, then
we set ```config["input_filepath"]: "networks/binarynetworks/"```, ```config["name"]: "admission"```, and ```config["filetype"]: "net"```.


You can also run RunCompiler with the network file as an argument. In this mode, you can choose to supply either one (network_file) or four (network_file, id, root, threshold) arguments. Both modes will pull all relevant config information from the file itself, but the first mode makes some guesses and defaults for the additional arguments that the second mode would have supplied:

- id will be 1
- root will be the first root in the list
- threshold will be the second ("no" case) probability in the CPT of the root.

#### Assumptions

The underlying Bayesian Network should be completely binary (all interal nodes and leaves are binary). Furthermore, there should be no hard CPTs (no 0/1 values in CPTS).

If you run with the network_file version, this will be checked automatically, and Exception if the network is not binary and soft. I make no guarantees to the time efficiency of this check...

### Output Format

The output of the program will be 4 files:

- ODD file: The ODD representation of the decision function of the BNC
- SDD file: The SDD representation of the decision function of the BNC
- vtree file: The vtree accompanying the SDD
- variable description file: A description of the variables of the ODD/SDD (includes variable order, metadata)

The ODD file will be written at ```config["output_filepath"] + config["name"] + "_" + config["id"] + ".odd"```

The SDD file will be written at ```config["output_filepath"] + config["name"] + "_" + config["id"] + ".sdd"```

The vtree file will be written at ```config["output_filepath"] + config["name"] + "_" + config["id"] + ".vtree"```

The variable description file will be written at ```config["output_filepath"] + config["name"] + "_" + config["id"] + ".txt"```

If running with the network_file version, a .json config file will also be produced in the same output folder, with the same form as those above, for the selected root.

### Running BNC_SDD

```
./run
```

modify run to change the usage of RunCompiler
There is also a ConfigGenerator that one can run to pull all network files from a directory and generate the config files for *all* roots in those networks. This uses the cpt probability method described above to determine the threshold.
Both of these have examples in the run file

### Further Questions

Contact us at
```
Andy Shih: andyshih@cs.ucla.edu
Arthur Choi: aychoi@cs.ucla.edu
Adnan Darwiche: darwiche@cs.ucla.edu
```
