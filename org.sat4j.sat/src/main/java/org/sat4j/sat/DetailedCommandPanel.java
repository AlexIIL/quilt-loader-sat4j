/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.ICDCLLogger;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearnedConstraintsEvaluationType;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SimplificationType;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.RandomWalkDecorator;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.core.IPBCDCLSolver;
import org.sat4j.pb.orders.RandomWalkDecoratorObjective;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ConflictDepthTracing;
import org.sat4j.tools.ConflictLevelTracing;
import org.sat4j.tools.DecisionTracing;
import org.sat4j.tools.FileBasedVisualizationTool;
import org.sat4j.tools.HeuristicsTracing;
import org.sat4j.tools.LearnedClausesSizeTracing;
import org.sat4j.tools.LearnedTracing;
import org.sat4j.tools.MultiTracing;
import org.sat4j.tools.SpeedTracing;


/**
 * 
 * This panel contains buttons that control restart and clean on solver.
 * It also displays history of commands.
 * 
 * @author sroussel
 *
 */
public class DetailedCommandPanel extends JPanel implements ICDCLLogger,SearchListener{


	private static final long serialVersionUID = 1L;

	private static final EmptyBorder border5 = new EmptyBorder(5,5,5,5);

	private String ramdisk;

	private RemoteControlStrategy telecomStrategy;
	private RandomWalkDecorator randomWalk;
	private ICDCL solver;
	private Reader reader;
	private IProblem problem;
	private boolean optimizationMode=false;

	private String solverInLine;
	private String[] commandLines;

	private boolean firstStart;

	private SolverVisualisation visu;

	//	private boolean useCustomizedSolver;
	private StartSolverEnum startConfig;

	private Thread solveurThread;

	private StringWriter stringWriter;

	private MyTabbedPane tabbedPane;

	private JPanel aboutSolverPanel;
	private JTextArea textArea;

	private JPanel instancePanel;
	private final static String INSTANCE_PANEL = "Instance";
	private JLabel instanceLabel;
	private final static String INSTANCE  = "Path to instance: ";
	private JTextField instancePathField;
	private String instancePath;
	private JButton browseButton;
	private final static String BROWSE = "Browse";

	private String whereToWriteFiles;


	private final static String MINISAT_PREFIX = "minisat";
	private final static String PB_PREFIX = "pb";
	private JPanel choixSolverPanel;
	private final static String CHOIX_SOLVER_PANEL = "Solver";
	private JLabel choixSolver;
	private final static String CHOIX_SOLVER  = "Choose solver: ";
	private String selectedSolver;
	private JComboBox listeSolvers;

	private final static String OPTMIZATION_MODE = "Use optimization mode";
	private JCheckBox optimisationModeCB;

	//	private JCheckBox useCustomizedSolverCB;
	//	private final static String USE_CUSTOMIZED_SOLVER = "Use customized solver";

	private JRadioButton solverLineParamLineRadio;
	private JRadioButton solverLineParamRemoteRadio;
	private JRadioButton solverListParamListRadio;
	private JRadioButton solverListParamRemoteRadio;
	private ButtonGroup solverConfigGroup;

	private final static String SOLVER_LINE_PARAM_LINE_CONFIG = "Start customized solver as given in command line";
	private final static String SOLVER_LINE_PARAM_REMOTE_CONFIG = "Start customized solver as given in command line with configuration given in the remote";
	private final static String SOLVER_LIST_PARAM_LIST_CONFIG = "Start solver as chosen in list with its default configuration";
	private final static String SOLVER_LIST_PARAM_REMOTE_CONFIG = "Start solver as chosen in list with configuration given in the remote";

	private JLabel chooseStartConfigLabel;
	private final static String CHOOSE_START_CONFIG = "Choose start configuration : ";

	private JButton startStopButton;
	private static final String START = "Start";
	private static final String STOP = "Stop";

	private JButton pauseButton;
	private static final String PAUSE = "Pause";
	private static final String RESUME = "Resume";
	//	private boolean isInterrupted;

	private final static String RESTART_PANEL = "Restart strategy";	
	private final static String RESTART = "Restart";

	private JPanel restartPanel;

	private JPanel restartPropertiesPanel;
	private JPanel restartButtonPanel;

	private JLabel chooseRestartStrategyLabel;
	private final static String CHOOSE_RESTART_STRATEGY = "Choose restart strategy: ";

	private final static String NO_PARAMETER_FOR_THIS_STRATEGY = "No paramaters for this strategy";
	private JLabel noParameterLabel;

	private JComboBox listeRestarts;
	private String currentRestart;
	//	private final static String RESTART_NO_STRATEGY = "No strategy";
	private final static String RESTART_DEFAULT = "NoRestarts";
	private final static String RESTART_STRATEGY_CLASS = "org.sat4j.minisat.core.RestartStrategy";
	private final static String RESTART_PATH="org.sat4j.minisat.restarts";


	//	private JPanel lubyPanel;
	private JLabel factorLabel;
	private final static String FACTOR = "Factor: ";
	private JTextField factorField;


	private JButton restartButton;

	private JPanel rwPanel;

	private JLabel probaRWLabel;
	private JTextField probaRWField;

	private JButton applyRWButton;

	private final static String RW_PANEL = "Random Walk";
	private final static String RW_LABEL = "Probabilty : ";
	private final static String RW_APPLY = "Apply";


	private JPanel cleanPanel;
	private final static String CLEAN_PANEL = "Learned Constraint Deletion Strategy";

	private JSlider cleanSlider;

	private final static String EVALUATION_TYPE = "Clauses evaluation type";
	private final static String ACTIVITY_BASED = "Activity";
	private final static String LBD_BASED = "LBD";
	private final static String LBD2_BASED = "LBD 2";
	private JLabel evaluationLabel;
	private ButtonGroup evaluationGroup;
	private JRadioButton activityRadio;
	private JRadioButton lbdRadio;
	private JRadioButton lbd2Radio;

	private JButton cleanAndEvaluationApplyButton;

	private JButton cleanButton;
	private final static String CLEAN = "Clean now";
	private final static String MANUAL_CLEAN = "Manual clean: ";
	private JLabel manualCleanLabel;

	private JLabel speedLabel;
	private JLabel speedNameLabel;
	private final static String SPEED = "Speed :";
	private JLabel speedUnitLabel;
	private final static String SPEED_UNIT = " propagations per second";

	private final JLabel deleteClauseLabel = new JLabel(DELETE_CLAUSES);
	private final static String DELETE_CLAUSES = "Automated clean: ";

	private Hashtable<Integer, JLabel> cleanValuesTable;
	private final JLabel clean5000Label = new JLabel(CLEAN_5000);
	private final JLabel clean10000Label = new JLabel(CLEAN_10000);
	private final JLabel clean20000Label = new JLabel(CLEAN_20000);
	private final JLabel clean50000Label = new JLabel(CLEAN_50000);
	private final JLabel clean100000Label = new JLabel(CLEAN_100000);
	private final JLabel clean500000Label = new JLabel(CLEAN_500000);
	private final static int[] cleanValues ={5000,10000,20000,50000,100000,500000};
	private final static int CLEAN_MIN = 0;
	private final static int CLEAN_MAX = 5;
	private final static int CLEAN_INIT = 1;
	private final static int CLEAN_SPACE = 1;

	private final static String CLEAN_5000 = "5000";
	private final static String CLEAN_10000 = "10000";
	private final static String CLEAN_20000 = "20000";
	private final static String CLEAN_50000 = "50000";
	private final static String CLEAN_100000 = "100000";
	private final static String CLEAN_500000 = "500000";

	private JCheckBox cleanUseOriginalStrategyCB;
	private final static String USE_ORIGINAL_STRATEGY = "Use solver's original deletion strategy";



	private JPanel phasePanel;
	private final static String PHASE_PANEL = "Phase Strategy";

	private String currentPhaseSelectionStrategy;

	private JComboBox phaseList;
	private JLabel phaseListLabel;
	private final static String PHASE_STRATEGY = "Choose phase strategy :";

	private JButton phaseApplyButton;
	private final static String PHASE_APPLY = "Apply";

	private final static String PHASE_STRATEGY_CLASS = "org.sat4j.minisat.core.IPhaseSelectionStrategy";
	private final static String PHASE_PATH_SAT="org.sat4j.minisat.orders";


	private JPanel simplifierPanel;
	private final static String SIMPLIFIER_PANEL = "Simplification strategy";
	private final static String SIMPLIFICATION_APPLY = "Apply";
	private final static String SIMPLIFICATION_NO = "No reason simplification";
	private final static String SIMPLIFICATION_SIMPLE = "Simple reason simplification";
	private final static String SIMPLIFICATION_EXPENSIVE = "Expensive reason simplification";

	private JButton simplificationApplyButton;
	private ButtonGroup simplificationGroup;
	private JRadioButton simplificationNoRadio;
	private JRadioButton simplificationSimpleRadio;
	private JRadioButton simplificationExpensiveRadio;


	private JPanel hotSolverPanel;
	private final static String HOT_SOLVER_PANEL = "Hot solver";
	private JCheckBox keepSolverHotCB;
	private final static String KEEP_SOLVER_HOT = "Keep solver hot";
	private JButton applyHotSolver;
	private final static String HOT_APPLY = "Apply";


	private JTextArea console;
	private JScrollPane scrollPane;

	private boolean isPlotActivated;

	private Process gnuplotProcess;

	private VisuPreferences visuPreferences;

	private boolean gnuplotBased = false;
	private boolean chartBased = false;
	
	private RemoteControlFrame frame;

	public DetailedCommandPanel(String filename, RemoteControlFrame frame){
		this(filename,"",frame);
	}

	public DetailedCommandPanel(String filename, String ramdisk, RemoteControlFrame frame){
		this(filename,ramdisk,null,frame);
	}

	public DetailedCommandPanel(String filename, String ramdisk, ICDCL solver, RemoteControlFrame frame){
		super();

		this.frame = frame;
		
		this.visuPreferences = new VisuPreferences();

		this.telecomStrategy = new RemoteControlStrategy(this);
		this.instancePath=filename;
		this.ramdisk = ramdisk;


		this.solver=solver;

		this.isPlotActivated=false;

		if(this.solver!=null)
			startConfig=StartSolverEnum.SOLVER_LINE_PARAM_LINE;
		else 
			startConfig=StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;

		this.firstStart=true;


		this.setPreferredSize(new Dimension(750,800));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		createInstancePanel();
		createChoixSolverPanel();
		createRestartPanel();
		createRWPanel();
		createCleanPanel();
		createPhasePanel();
		createSimplifierPanel();
		createHotSolverPanel();

		console = new JTextArea();

		scrollPane = new JScrollPane(console);

		//scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(400,200));
		scrollPane.getVerticalScrollBar().setValue(
				scrollPane.getVerticalScrollBar().getMaximum());
		//	scrollPane.setAutoscrolls(true);

		initFactorParam();

		restartPropertiesPanel.setPreferredSize(new Dimension(100,50));


		tabbedPane = new MyTabbedPane();

		JPanel solverBigPanel = new JPanel();
		solverBigPanel.setLayout(new BoxLayout(solverBigPanel, BoxLayout.Y_AXIS));
		solverBigPanel.add(instancePanel);
		solverBigPanel.add(choixSolverPanel);

		tabbedPane.addTab("Solver", null, solverBigPanel, "instance & solver options");


		JPanel restartBigPanel = new JPanel();
		restartBigPanel.setLayout(new BoxLayout(restartBigPanel, BoxLayout.Y_AXIS));
		restartBigPanel.add(restartPanel);
		//restartBigPanel.add(hotSolverPanel);

		tabbedPane.addTab("Restart",null,restartBigPanel, "restart strategy & options");

		JPanel rwPhaseBigPanel = new JPanel();
		rwPhaseBigPanel.setLayout(new BoxLayout(rwPhaseBigPanel, BoxLayout.Y_AXIS));
		rwPhaseBigPanel.add(rwPanel);
		rwPhaseBigPanel.add(phasePanel);
		rwPhaseBigPanel.add(hotSolverPanel);

		tabbedPane.addTab("Heuristics",null,rwPhaseBigPanel, "random walk and phase strategy");

		JPanel clausesBigPanel = new JPanel();
		clausesBigPanel.setLayout(new BoxLayout(clausesBigPanel, BoxLayout.Y_AXIS));
		clausesBigPanel.add(cleanPanel);
		clausesBigPanel.add(simplifierPanel);

		tabbedPane.addTab("Learned Constraints",null,clausesBigPanel, "deletion and simplification strategy");

		aboutSolverPanel = new JPanel();
		textArea = new JTextArea("No solver is running at the moment");
		textArea.setColumns(50);
		aboutSolverPanel.add(textArea);

		tabbedPane.addTab("About Solver",null,aboutSolverPanel, "information about solver");

		this.add(tabbedPane);
		this.add(scrollPane);



		setRestartPanelEnabled(false);
		setRWPanelEnabled(false);
		setCleanPanelEnabled(false);
		setPhasePanelEnabled(false);
		setSimplifierPanelEnabled(false);
		setKeepSolverHotPanelEnabled(false);

		updateWriter();
	}



	public void createInstancePanel(){
		instancePanel = new JPanel();

		instancePanel.setName(INSTANCE_PANEL);
		instancePanel.setBorder(new CompoundBorder(new TitledBorder(null, instancePanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		instancePanel.setLayout(new BorderLayout(0,0));

		instanceLabel = new JLabel(INSTANCE);
		instancePathField = new JTextField(20);
		instancePathField.setText(instancePath);

		instanceLabel.setLabelFor(instancePathField);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(instanceLabel);
		tmpPanel1.add(instancePathField);

		browseButton = new JButton(BROWSE);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(browseButton);

		instancePanel.add(tmpPanel1,BorderLayout.CENTER);
		instancePanel.add(tmpPanel2,BorderLayout.SOUTH);


	}

	public void createChoixSolverPanel(){
		choixSolverPanel = new JPanel();

		choixSolverPanel.setName(CHOIX_SOLVER_PANEL);
		choixSolverPanel.setBorder(new CompoundBorder(new TitledBorder(null, choixSolverPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		choixSolverPanel.setLayout(new BorderLayout());

		choixSolver = new JLabel(CHOIX_SOLVER);
		updateListOfSolvers();


		optimisationModeCB = new JCheckBox(OPTMIZATION_MODE);
		optimisationModeCB.setSelected(optimizationMode);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(choixSolver);
		tmpPanel1.add(listeSolvers);
		tmpPanel1.add(optimisationModeCB);

		optimisationModeCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optimizationMode = optimisationModeCB.isSelected();
				log("use optimization mode: " + optimizationMode);
			}
		});

		startStopButton = new JButton(START);

		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(startStopButton.getText().equals(START)){
					//					launchSolver();
					launchSolverWithConfigs();
					pauseButton.setEnabled(true);
					setInstancePanelEnabled(false);
					setRestartPanelEnabled(true);
					setRWPanelEnabled(true);
					setCleanPanelEnabled(true);
					setCleanPanelOriginalStrategyEnabled(true);
					setPhasePanelEnabled(true);
					setChoixSolverPanelEnabled(false);
					setSimplifierPanelEnabled(true);
					setKeepSolverHotPanelEnabled(true);
					startStopButton.setText(STOP);
					getThis().paintAll(getThis().getGraphics());
					frame.setActivateTracingEditableUnderCondition(false);
				}
				else {

					//assert solveurThread!=null;
					((ISolver)problem).expireTimeout();
					pauseButton.setEnabled(false);
					log("Asked the solver to stop");
					setInstancePanelEnabled(true);
					setChoixSolverPanelEnabled(true);
					//					setRestartPanelEnabled(false);
					//					setRWPanelEnabled(false);
					//					setCleanPanelEnabled(false);
					//					setPhasePanelEnabled(false);
					//					setSimplifierPanelEnabled(false);
					//					setKeepSolverHotPanelEnabled(false);
					startStopButton.setText(START);
					getThis().paintAll(getThis().getGraphics());
					frame.setActivateTracingEditable(true);
					
				}
			}
		});

		pauseButton = new JButton(PAUSE);
		pauseButton.setEnabled(false);

		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(pauseButton.getText().equals(PAUSE)){
					pauseButton.setText(RESUME);
					telecomStrategy.setInterrupted(true);
				}
				else{
					pauseButton.setText(PAUSE);
					telecomStrategy.setInterrupted(false);
				}

			}
		});


		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.setLayout(new FlowLayout());
		tmpPanel2.add(startStopButton);
		tmpPanel2.add(pauseButton);

		solverLineParamLineRadio = new JRadioButton(SOLVER_LINE_PARAM_LINE_CONFIG);
		solverLineParamRemoteRadio = new JRadioButton(SOLVER_LINE_PARAM_REMOTE_CONFIG);
		solverListParamRemoteRadio = new JRadioButton(SOLVER_LIST_PARAM_REMOTE_CONFIG);
		solverListParamListRadio = new JRadioButton(SOLVER_LIST_PARAM_LIST_CONFIG);

		solverConfigGroup = new ButtonGroup();
		solverConfigGroup.add(solverLineParamLineRadio);
		solverConfigGroup.add(solverLineParamRemoteRadio);
		solverConfigGroup.add(solverListParamListRadio);
		solverConfigGroup.add(solverListParamRemoteRadio);

		chooseStartConfigLabel = new JLabel(CHOOSE_START_CONFIG);

		JPanel tmpPanel3 = new JPanel();
		tmpPanel3.setLayout(new BoxLayout(tmpPanel3, BoxLayout.Y_AXIS));

		tmpPanel3.add(chooseStartConfigLabel);
		tmpPanel3.add(solverLineParamLineRadio);
		tmpPanel3.add(solverLineParamRemoteRadio);
		tmpPanel3.add(solverListParamListRadio);
		tmpPanel3.add(solverListParamRemoteRadio);


		solverLineParamLineRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverLineParamLineRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LINE_PARAM_LINE;
				}
			}
		});

		solverLineParamRemoteRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverLineParamRemoteRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LINE_PARAM_REMOTE;
				}
			}
		});

		solverListParamListRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverListParamListRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT;
				}
			}
		});

		solverListParamRemoteRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(solverListParamRemoteRadio.isSelected()){
					startConfig = StartSolverEnum.SOLVER_LIST_PARAM_REMOTE;
				}
			}
		});


		setChoixSolverPanelEnabled(true);

		if(solverInLine==null){
			solverLineParamLineRadio.setEnabled(false);
			solverLineParamRemoteRadio.setEnabled(false);
		}

		if(firstStart){
			solverLineParamRemoteRadio.setEnabled(false);
			solverListParamRemoteRadio.setEnabled(false);
		}

		choixSolverPanel.add(tmpPanel1,BorderLayout.NORTH);
		choixSolverPanel.add(tmpPanel3,BorderLayout.CENTER);
		choixSolverPanel.add(tmpPanel2,BorderLayout.SOUTH);

	}

	public String getStartStopText(){
		return startStopButton.getText();
	}

	public void createRestartPanel(){
		restartPanel = new JPanel();

		restartPanel.setName(RESTART_PANEL);
		restartPanel.setBorder(new CompoundBorder(new TitledBorder(null, restartPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		restartPanel.setLayout(new BorderLayout());

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		chooseRestartStrategyLabel = new JLabel(CHOOSE_RESTART_STRATEGY);

		listeRestarts = new JComboBox(getListOfRestartStrategies().toArray());	
		currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();
		listeRestarts.setSelectedItem(RESTART_DEFAULT);

		listeRestarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifyRestartParamPanel();
			}
		});

		tmpPanel1.add(chooseRestartStrategyLabel);
		tmpPanel1.add(listeRestarts);

		noParameterLabel = new JLabel(NO_PARAMETER_FOR_THIS_STRATEGY);

		Font newLabelFont=new Font(noParameterLabel.getFont().getName(),Font.ITALIC,noParameterLabel.getFont().getSize());

		noParameterLabel.setFont(newLabelFont);

		restartPropertiesPanel = new JPanel();
		restartPropertiesPanel.add(noParameterLabel);


		restartButton = new JButton(RESTART);

		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnRestart();
			}
		});

		restartButtonPanel = new JPanel();
		restartButtonPanel.add(restartButton);

		restartPanel.add(tmpPanel1,BorderLayout.NORTH);
		restartPanel.add(restartPropertiesPanel,BorderLayout.CENTER);
		restartPanel.add(restartButtonPanel,BorderLayout.SOUTH);

	}

	public void createRWPanel(){
		rwPanel = new JPanel();

		rwPanel.setName(RW_PANEL);
		rwPanel.setBorder(new CompoundBorder(new TitledBorder(null, rwPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		rwPanel.setLayout(new BorderLayout());

		probaRWLabel = new JLabel(RW_LABEL);
		probaRWField = new JTextField("0",10);

		probaRWLabel.setLabelFor(probaRWField);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		tmpPanel1.add(probaRWLabel);
		tmpPanel1.add(probaRWField);

		JPanel tmpPanel2 = new JPanel();
		applyRWButton = new JButton(RW_APPLY);

		applyRWButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnApplyRW();
			}
		});

		tmpPanel2.add(applyRWButton);

		rwPanel.add(tmpPanel1, BorderLayout.CENTER);
		rwPanel.add(tmpPanel2, BorderLayout.SOUTH);


	}

	public void createCleanPanel(){
		cleanPanel = new JPanel();

		cleanPanel.setName(CLEAN_PANEL);
		cleanPanel.setBorder(new CompoundBorder(new TitledBorder(null, cleanPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		cleanPanel.setLayout(new BorderLayout());


		cleanSlider = new JSlider(JSlider.HORIZONTAL,CLEAN_MIN,CLEAN_MAX,CLEAN_INIT);

		cleanSlider.setMajorTickSpacing(CLEAN_SPACE);
		cleanSlider.setPaintTicks(true);

		//Create the label table
		cleanValuesTable = new Hashtable<Integer, JLabel>();
		cleanValuesTable.put(new Integer(0),clean5000Label);
		cleanValuesTable.put(new Integer(1),clean10000Label);
		cleanValuesTable.put(new Integer(2),clean20000Label);
		cleanValuesTable.put(new Integer(3),clean50000Label);
		cleanValuesTable.put(new Integer(4),clean100000Label);
		cleanValuesTable.put(new Integer(5),clean500000Label);
		cleanSlider.setLabelTable(cleanValuesTable);

		cleanSlider.setPaintLabels(true);
		cleanSlider.setSnapToTicks(true);

		cleanSlider.setPreferredSize(new Dimension(400,50));

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.add(deleteClauseLabel);
		tmpPanel1.add(cleanSlider);

		JPanel tmpPanel4 = new JPanel();

		evaluationLabel = new JLabel(EVALUATION_TYPE);
		evaluationGroup = new ButtonGroup();
		activityRadio = new JRadioButton(ACTIVITY_BASED);
		lbdRadio = new JRadioButton(LBD_BASED);
		lbd2Radio = new JRadioButton(LBD2_BASED);

		evaluationGroup.add(activityRadio);
		evaluationGroup.add(lbdRadio);
		evaluationGroup.add(lbd2Radio);

		tmpPanel4.add(evaluationLabel);
		tmpPanel4.add(activityRadio);
		tmpPanel4.add(lbdRadio);
		tmpPanel4.add(lbd2Radio);


		cleanAndEvaluationApplyButton = new JButton("Apply changes");
		cleanAndEvaluationApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasChangedCleaningValue();
			}
		});

		JPanel tmpPanel5 = new JPanel();
		tmpPanel5.add(cleanAndEvaluationApplyButton);

		JPanel tmpPanel = new JPanel();
		tmpPanel.setBorder(new CompoundBorder(new TitledBorder(null, "", 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		tmpPanel.setLayout(new BorderLayout());

		tmpPanel.add(tmpPanel1,BorderLayout.NORTH);
		tmpPanel.add(tmpPanel4,BorderLayout.CENTER);
		tmpPanel.add(tmpPanel5,BorderLayout.SOUTH);

		JPanel tmpPanel2 = new JPanel();

		manualCleanLabel = new JLabel(MANUAL_CLEAN);

		cleanButton = new JButton(CLEAN);

		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnClean();
			}
		});


		tmpPanel2.add(manualCleanLabel);
		tmpPanel2.add(cleanButton);

		JPanel tmpPanel3 = new JPanel();
		cleanUseOriginalStrategyCB = new JCheckBox(USE_ORIGINAL_STRATEGY);
		cleanUseOriginalStrategyCB.setSelected(true);

		cleanUseOriginalStrategyCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnUseOriginalStrategy();
			}
		});

		tmpPanel3.add(cleanUseOriginalStrategyCB);


		JPanel tmpPanel6 = new JPanel();
		speedLabel = new JLabel("");
		speedNameLabel = new JLabel(SPEED);
		speedUnitLabel = new JLabel(SPEED_UNIT);

		tmpPanel6.add(speedNameLabel);
		tmpPanel6.add(speedLabel);
		tmpPanel6.add(speedUnitLabel);

		JPanel tmpPanel7 = new JPanel();
		tmpPanel7.setLayout(new BorderLayout());

		tmpPanel7.add(tmpPanel2,BorderLayout.SOUTH);
		tmpPanel7.add(tmpPanel6,BorderLayout.CENTER);


		cleanPanel.add(tmpPanel3,BorderLayout.NORTH);
		cleanPanel.add(tmpPanel7,BorderLayout.CENTER);
		cleanPanel.add(tmpPanel,BorderLayout.SOUTH);

	}

	public void createPhasePanel(){
		phasePanel = new JPanel();

		phasePanel.setName(PHASE_PANEL);
		phasePanel.setBorder(new CompoundBorder(new TitledBorder(null, phasePanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		phasePanel.setLayout(new BorderLayout());

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		phaseListLabel = new JLabel(PHASE_STRATEGY);

		phaseList = new JComboBox(getListOfPhaseStrategies().toArray());	
		currentPhaseSelectionStrategy = telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName();
		phaseList.setSelectedItem(currentPhaseSelectionStrategy);

		//		phaseList.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				modifyRestartParamPanel();
		//			}
		//		});

		tmpPanel1.add(phaseListLabel);
		tmpPanel1.add(phaseList);




		phaseApplyButton = new JButton(PHASE_APPLY);

		phaseApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnApplyPhase();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(phaseApplyButton);

		phasePanel.add(tmpPanel1,BorderLayout.CENTER);
		phasePanel.add(tmpPanel2,BorderLayout.SOUTH);
	}

	public void createSimplifierPanel(){
		simplifierPanel = new JPanel();

		simplifierPanel.setName(SIMPLIFIER_PANEL);
		simplifierPanel.setBorder(new CompoundBorder(new TitledBorder(null, simplifierPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		simplifierPanel.setLayout(new BorderLayout());

		//		simplificationRadio = new Radio
		simplificationGroup = new ButtonGroup();
		simplificationExpensiveRadio = new JRadioButton(SIMPLIFICATION_EXPENSIVE);
		simplificationNoRadio = new JRadioButton(SIMPLIFICATION_NO);
		simplificationSimpleRadio = new JRadioButton(SIMPLIFICATION_SIMPLE);

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new BoxLayout(tmpPanel1, BoxLayout.Y_AXIS));

		simplificationGroup.add(simplificationNoRadio);
		simplificationGroup.add(simplificationSimpleRadio);
		simplificationGroup.add(simplificationExpensiveRadio);


		tmpPanel1.add(simplificationNoRadio);
		tmpPanel1.add(simplificationSimpleRadio);
		tmpPanel1.add(simplificationExpensiveRadio);

		simplificationApplyButton = new JButton(SIMPLIFICATION_APPLY);

		simplificationApplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnApplySimplification();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(simplificationApplyButton);

		simplifierPanel.add(tmpPanel1,BorderLayout.NORTH);
		simplifierPanel.add(tmpPanel2,BorderLayout.SOUTH);

	}

	public void createHotSolverPanel(){
		hotSolverPanel = new JPanel();
		hotSolverPanel.setName(HOT_SOLVER_PANEL);

		hotSolverPanel.setBorder(new CompoundBorder(new TitledBorder(null, hotSolverPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		hotSolverPanel.setLayout(new BorderLayout());

		keepSolverHotCB = new JCheckBox(KEEP_SOLVER_HOT);
		hotSolverPanel.add(keepSolverHotCB,BorderLayout.CENTER);

		JPanel tmpPanel = new JPanel();

		applyHotSolver = new JButton(HOT_APPLY);
		tmpPanel.add(applyHotSolver);
		hotSolverPanel.add(tmpPanel,BorderLayout.SOUTH);


		applyHotSolver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				solver.setKeepSolverHot(keepSolverHotCB.isSelected());
				if(keepSolverHotCB.isSelected()){
					log("Keep hot solver is now activated");
				}
				else{
					log("Keep hot solver is now desactivated");
				}
			}
		});

	}

	public void initFactorParam(){
		//		lubyPanel = new JPanel();
		//		//		lubyPanel.setLayout(new FlowLayout());

		factorLabel = new JLabel(FACTOR);
		factorField = new JTextField(LubyRestarts.DEFAULT_LUBY_FACTOR+"",5);
		//factorField.setMargin(new Insets(0, 0, 0, 0));
		//factorLabel.setLabelFor(factorField);

		//		lubyPanel.add(factorLabel);
		//		lubyPanel.add(factorField);

	}

	public void setOptimisationMode(boolean optimizationMode){
		this.optimizationMode = optimizationMode;
		optimisationModeCB.setSelected(optimizationMode);
	}

	//	public void launchSolver(){
	//
	//
	//		if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT) || startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_REMOTE))
	//		{
	//			selectedSolver = (String)listeSolvers.getSelectedItem();
	//			String[] partsSelectedSolver = selectedSolver.split("\\.");
	//
	//			assert partsSelectedSolver.length==2;
	//			assert (partsSelectedSolver[0].equals(MINISAT_PREFIX) || partsSelectedSolver[0].equals(PB_PREFIX)) ;
	//
	//			ASolverFactory factory;
	//
	//			if(partsSelectedSolver[0].equals(MINISAT_PREFIX)){
	//				factory = org.sat4j.minisat.SolverFactory.instance();
	//			}
	//			else{
	//				factory = org.sat4j.pb.SolverFactory.instance();
	//			}
	//			solver = (ICDCL)factory.createSolverByName(partsSelectedSolver[1]);
	//			//log(solver.toString());
	//		}
	//
	//
	//		if(firstStart || startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT) || startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_REMOTE)){
	//			telecomStrategy.setSolver(solver);
	//			telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
	//			solver.setRestartStrategy(telecomStrategy);
	//
	//		}
	//
	//		currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();
	//
	//
	//
	//
	//
	//		//		if(randomWalk==null){
	//		//			double proba=0;
	//		//			probaRWField.setText("0");
	//		//			rwPanel.repaint();
	//		//			randomWalk = new RandomWalkDecorator((VarOrderHeap)((Solver)solver).getOrder(), proba);
	//		//		}
	//
	//
	//		IOrder order = solver.getOrder();
	//		//		if(order instanceof RandomWalkDecoratorObjective && )
	//
	//
	//		if(solver.getOrder() instanceof RandomWalkDecorator){
	//			randomWalk = (RandomWalkDecorator)solver.getOrder();
	//			if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT)){
	//				randomWalk.setProbability(0);
	//				probaRWField.setText("0");
	//				rwPanel.repaint();
	//			}
	//			else if(startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_LINE)){
	//				if(solverInLine.getOrder() instanceof RandomWalkDecorator)
	//					randomWalk = (RandomWalkDecorator)solverInLine.getOrder();
	//				else
	//					randomWalk = new RandomWalkDecorator((VarOrderHeap)((Solver)solver).getOrder(), 0);
	//			}
	//			else{
	//				double proba = Double.parseDouble(probaRWField.getText());
	//				randomWalk.setProbability(proba);
	//			}
	//		}
	//		else{
	//			randomWalk = new RandomWalkDecorator((VarOrderHeap)((Solver)solver).getOrder(), 0);
	//		}
	//		//		
	//		//		if (optimizationMode
	//		//				&& order instanceof VarOrderHeapObjective) {
	//		//			randomWalk = new RandomWalkDecoratorObjective(
	//		//					(VarOrderHeapObjective) order, 0);
	//		//		} else {
	//		//			randomWalk = new RandomWalkDecorator((VarOrderHeap) order, 0);
	//		//		}
	//
	//		solver.setOrder(randomWalk);
	//
	//		//		if(solver.getOrder() instanceof VarOrderHeapObjective){
	//		//			randomWalk = (RandomWalkDecoratorObjective)solver.getOrder();
	//		//			randomWalk.setProbability(0);
	//		//			probaRWField.setText("0");
	//		//			rwPanel.repaint();
	//		//		}
	//		//		else
	//		//		if(solver.getOrder() instanceof RandomWalkDecorator){
	//		//			randomWalk = (RandomWalkDecorator)solver.getOrder();
	//		//			randomWalk.setProbability(0);
	//		//			probaRWField.setText("0");
	//		//			rwPanel.repaint();
	//		//		}
	//		//		else if(!optimizationMode){
	//		//			randomWalk = new RandomWalkDecorator((VarOrderHeap)((Solver)solver).getOrder(), 0);
	//		//		}
	//		//		else {
	//		//			randomWalk = new RandomWalkDecoratorObjective((VarOrderHeapObjective) ((Solver)solver).getOrder(), 0);
	//		//		}
	//
	//		//		solver.setOrder(randomWalk);
	//
	//		if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT) || startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_LINE))
	//
	//			telecomStrategy.setPhaseSelectionStrategy(solver.getOrder().getPhaseSelectionStrategy());
	//		currentPhaseSelectionStrategy = telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName();
	//
	//		solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);
	//
	//
	//		if(solver.getSimplifier().toString().equals(SIMPLIFICATION_EXPENSIVE)){
	//			simplificationExpensiveRadio.setSelected(true);
	//		}
	//		else if(solver.getSimplifier().toString().equals(SIMPLIFICATION_SIMPLE)){
	//			simplificationSimpleRadio.setSelected(true);
	//		}
	//		else{
	//			simplificationNoRadio.setSelected(true);
	//		}
	//
	//		phaseList.setSelectedItem(currentPhaseSelectionStrategy);
	//		phasePanel.repaint();
	//
	//		updateRestartStrategyPanel();
	//
	//		//pbSolver.setNeedToReduceDB(true);
	//
	//
	//
	//
	//		whereToWriteFiles = instancePath;
	//
	//		if(ramdisk.length()>0){
	//			String[] instancePathSplit= instancePath.split("/");
	//			whereToWriteFiles = ramdisk+"/"+ instancePathSplit[instancePathSplit.length-1];
	//
	//		}
	//
	//		solver.setVerbose(true);
	//
	//		List<SearchListener> listeners = new ArrayList<SearchListener>();
	//
	//
	//		if(gnuplotBased){
	//			if(visuPreferences.isDisplayClausesEvaluation()){
	//				listeners.add(new LearnedTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-learned")));
	//			}
	//			if(visuPreferences.isDisplayClausesSize()){
	//				listeners.add(new LearnedClausesSizeTracing(
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size"), 
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size-restart")));
	//			}
	//			if(visuPreferences.isDisplayConflictsDecision()){
	//				listeners.add(new ConflictLevelTracing(
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level"), 
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level-restart")));
	//			}
	//			if(visuPreferences.isDisplayConflictsTrail()){
	//				listeners.add(new ConflictDepthTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth"),
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth-restart")));
	//			}
	//
	//			if(visuPreferences.isDisplayDecisionIndexes()){
	//				listeners.add(new DecisionTracing(
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-pos"),
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-neg"),
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-restart")));
	//			}
	//
	//			if(visuPreferences.isDisplaySpeed()){
	//				listeners.add(new SpeedTracing(
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-speed"),
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-speed-clean"), 
	//						new FileBasedVisualizationTool(whereToWriteFiles + "-speed-restart")));
	//			}
	//			if(visuPreferences.isDisplayVariablesEvaluation()){
	//				listeners.add(new HeuristicsTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-heuristics")));
	//			}
	//		}
	//
	//		else if(chartBased){
	//
	//			SolverVisualisation visu = new SolverVisualisation(visuPreferences);
	//
	//			visu.setnVar(solver.nVars());
	//			if(visuPreferences.isDisplayClausesEvaluation()){
	//				listeners.add(new LearnedTracing(new ChartBasedVisualizationTool(visu.getClausesEvaluationTrace())));
	//			}
	//			if(visuPreferences.isDisplayClausesSize()){
	//				listeners.add(new LearnedClausesSizeTracing(
	//						new ChartBasedVisualizationTool(visu.getLearnedClausesSizeTrace()),
	//						new ChartBasedVisualizationTool(visu.getLearnedClausesSizeRestartTrace())));
	//			}
	//			if(visuPreferences.isDisplayConflictsDecision()){
	//				listeners.add(new ConflictLevelTracing(
	//						new ChartBasedVisualizationTool(visu.getConflictLevelTrace()), 
	//						new ChartBasedVisualizationTool(visu.getConflictLevelRestartTrace())));
	//			}
	//			if(visuPreferences.isDisplayConflictsTrail()){
	//				listeners.add(new ConflictDepthTracing(
	//						new ChartBasedVisualizationTool(visu.getConflictDepthTrace()),
	//						new ChartBasedVisualizationTool(visu.getConflictDepthRestartTrace())));
	//			}
	//			if(visuPreferences.isDisplayDecisionIndexes()){
	//				listeners.add(new DecisionTracing(
	//						new ChartBasedVisualizationTool(visu.getPositiveDecisionTrace()),
	//						new ChartBasedVisualizationTool(visu.getNegativeDecisionTrace()),
	//						new ChartBasedVisualizationTool(new TraceComposite(visu.getRestartPosDecisionTrace(),visu.getRestartNegDecisionTrace()))));
	//			}
	//			if(visuPreferences.isDisplaySpeed()){
	//				listeners.add(new SpeedTracing(
	//						new ChartBasedVisualizationTool(visu.getSpeedTrace()), 
	//						new ChartBasedVisualizationTool(visu.getSpeedCleanTrace()), 
	//						new ChartBasedVisualizationTool(visu.getSpeedRestartTrace())));
	//			}
	//			if(visuPreferences.isDisplayVariablesEvaluation()){
	//				listeners.add(new HeuristicsTracing(new ChartBasedVisualizationTool(visu.getHeuristicsTrace())));
	//			}
	//		}
	//
	//		listeners.add(this);
	//
	//		solver.setSearchListener(new MultiTracing(listeners));
	//
	//		solver.setLogger(this);
	//
	//		reader = createReader(solver, instancePath);
	//
	//
	//		try{
	//			problem = reader.parseInstance(instancePath);
	//		} catch (FileNotFoundException e) {
	//			e.printStackTrace();
	//		} catch (ParseFormatException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (ContradictionException e) {
	//			log("Unsatisfiable (trivial)!");
	//		}
	//
	//		boolean optimisation=false;
	//		if(reader instanceof PBInstanceReader){
	//			optimisation = ((PBInstanceReader)reader).hasObjectiveFunction();
	//			if(optimisation){
	//				problem = new OptToPBSATAdapter(new PseudoOptDecorator((IPBCDCLSolver)solver));
	//			}
	//		}
	//
	//
	//		log("# Started solver " + solver.getClass().getSimpleName());
	//		log("# on instance " + instancePath);
	//		log("# Optimisation = " + optimisation);
	//		log("# Restart strategy = " + solver.getRestartStrategy().getClass().getSimpleName());
	//		log("# Random walk probability = " +randomWalk.getProbability());
	//		//log("# Number of conflicts before cleaning = " + nbConflicts);
	//
	//
	//		solveurThread = new Thread() {
	//			public void run() {
	//				//Thread thisThread = Thread.currentThread();
	//				//				if(shouldStop){
	//				//					System.out.println("coucou");
	//				//				}
	//				//				while(!shouldStop){
	//				try{
	//					stringWriter = new StringWriter();
	//					if(problem.isSatisfiable()){
	//						log("Satisfiable !");
	//						if(problem instanceof OptToPBSATAdapter){
	//							log(((OptToPBSATAdapter)problem).getCurrentObjectiveValue()+"");
	//							reader.decode(((OptToPBSATAdapter)problem).model(new PrintWriter(stringWriter)), new PrintWriter(stringWriter));
	//						}
	//						else
	//							reader.decode(problem.model(),new PrintWriter(stringWriter));
	//						log(stringWriter.toString());
	//					}
	//					else{
	//						log("Unsatisfiable !");
	//					}
	//				} catch (TimeoutException e) {
	//					log("Timeout, sorry!");      
	//				}
	//				//log("Solver has stopped");
	//				//				}
	//			}
	//		};
	//		solveurThread.start();
	//
	//
	//		if(isPlotActivated && gnuplotBased){
	//			traceGnuplot();
	//		}
	//
	//	}


	public void launchSolverWithConfigs(){


		if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_DEFAULT)){
			selectedSolver = (String)listeSolvers.getSelectedItem();
			String[] partsSelectedSolver = selectedSolver.split("\\.");

			assert partsSelectedSolver.length==2;
			assert (partsSelectedSolver[0].equals(MINISAT_PREFIX) || partsSelectedSolver[0].equals(PB_PREFIX)) ;

			ASolverFactory factory;

			if(partsSelectedSolver[0].equals(MINISAT_PREFIX)){
				factory = org.sat4j.minisat.SolverFactory.instance();
			}
			else{
				factory = org.sat4j.pb.SolverFactory.instance();
			}
			solver = (ICDCL)factory.createSolverByName(partsSelectedSolver[1]);

			telecomStrategy.setSolver(solver);
			telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
			solver.setRestartStrategy(telecomStrategy);

			currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();

			IOrder order = solver.getOrder();

			double proba = 0;

			if(optimizationMode){
				if(order instanceof RandomWalkDecoratorObjective){
					randomWalk = (RandomWalkDecorator)order;
					proba = randomWalk.getProbability();
				}
				else if(order instanceof VarOrderHeapObjective){
					randomWalk = new RandomWalkDecoratorObjective((VarOrderHeapObjective) order, 0);
				}
			}
			else if(solver.getOrder() instanceof RandomWalkDecorator){
				randomWalk = (RandomWalkDecorator)order;
				proba = randomWalk.getProbability();
			}
			else{
				randomWalk = new RandomWalkDecorator((VarOrderHeap)order, 0);
			}

			randomWalk.setProbability(proba);
			probaRWField.setText(proba+"");
			rwPanel.repaint();

			solver.setOrder(randomWalk);



			telecomStrategy.setPhaseSelectionStrategy(solver.getOrder().getPhaseSelectionStrategy());
			currentPhaseSelectionStrategy = telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName();

			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);


			if(solver.getSimplifier().toString().equals(SIMPLIFICATION_EXPENSIVE)){
				simplificationExpensiveRadio.setSelected(true);
			}
			else if(solver.getSimplifier().toString().equals(SIMPLIFICATION_SIMPLE)){
				simplificationSimpleRadio.setSelected(true);
			}
			else{
				simplificationNoRadio.setSelected(true);
			}

			phaseList.setSelectedItem(currentPhaseSelectionStrategy);
			phasePanel.repaint();

			updateRestartStrategyPanel();

		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LIST_PARAM_REMOTE)){
			selectedSolver = (String)listeSolvers.getSelectedItem();
			String[] partsSelectedSolver = selectedSolver.split("\\.");

			assert partsSelectedSolver.length==2;
			assert (partsSelectedSolver[0].equals(MINISAT_PREFIX) || partsSelectedSolver[0].equals(PB_PREFIX)) ;

			ASolverFactory factory;

			if(partsSelectedSolver[0].equals(MINISAT_PREFIX)){
				factory = org.sat4j.minisat.SolverFactory.instance();
			}
			else{
				factory = org.sat4j.pb.SolverFactory.instance();
			}
			solver = (ICDCL)factory.createSolverByName(partsSelectedSolver[1]);

			telecomStrategy.setSolver(solver);

			solver.setRestartStrategy(telecomStrategy);
			solver.setOrder(randomWalk);
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);


			hasClickedOnRestart();

			hasClickedOnApplyRW();

			hasClickedOnApplyPhase();

			hasClickedOnApplySimplification();

			updateRestartStrategyPanel();

		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_LINE)){

			telecomStrategy.setSolver(solver);
			telecomStrategy.setRestartStrategy(solver.getRestartStrategy());
			solver.setRestartStrategy(telecomStrategy);

			currentRestart = telecomStrategy.getRestartStrategy().getClass().getSimpleName();

			updateRestartStrategyPanel();

			IOrder order = solver.getOrder();

			double proba = 0;

			if(optimizationMode){
				if(order instanceof RandomWalkDecoratorObjective){
					randomWalk = (RandomWalkDecorator)order;
					proba = randomWalk.getProbability();
				}
				else if(order instanceof VarOrderHeapObjective){
					randomWalk = new RandomWalkDecoratorObjective((VarOrderHeapObjective) order, 0);
				}
			}
			else if(solver.getOrder() instanceof RandomWalkDecorator){
				randomWalk = (RandomWalkDecorator)order;
				proba = randomWalk.getProbability();
			}
			else{
				randomWalk = new RandomWalkDecorator((VarOrderHeap)order, 0);
			}

			randomWalk.setProbability(proba);
			probaRWField.setText(proba+"");
			rwPanel.repaint();

			solver.setOrder(randomWalk);



			telecomStrategy.setPhaseSelectionStrategy(solver.getOrder().getPhaseSelectionStrategy());
			currentPhaseSelectionStrategy = telecomStrategy.getPhaseSelectionStrategy().getClass().getSimpleName();

			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);

			phaseList.setSelectedItem(currentPhaseSelectionStrategy);


			if(solver.getSimplifier().toString().equals(SIMPLIFICATION_EXPENSIVE)){
				simplificationExpensiveRadio.setSelected(true);
			}
			else if(solver.getSimplifier().toString().equals(SIMPLIFICATION_SIMPLE)){
				simplificationSimpleRadio.setSelected(true);
			}
			else{
				simplificationNoRadio.setSelected(true);
			}


			phasePanel.repaint();



		}

		else if(startConfig.equals(StartSolverEnum.SOLVER_LINE_PARAM_REMOTE)){
			//			solver = solverInLine;

			solver.setRestartStrategy(telecomStrategy);
			solver.setOrder(randomWalk);
			solver.getOrder().setPhaseSelectionStrategy(telecomStrategy);


			hasClickedOnRestart();

			hasClickedOnApplyRW();

			hasClickedOnApplyPhase();

			hasClickedOnApplySimplification();

			updateRestartStrategyPanel();



		}



		whereToWriteFiles = instancePath;

		if(ramdisk.length()>0){
			String[] instancePathSplit= instancePath.split("/");
			whereToWriteFiles = ramdisk+"/"+ instancePathSplit[instancePathSplit.length-1];

		}

		solver.setVerbose(true);

		initSearchListeners();

		solver.setLogger(this);

		reader = createReader(solver, instancePath);


		try{
			problem = reader.parseInstance(instancePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			log("Unsatisfiable (trivial)!");
		}

		boolean optimisation=false;
		if(reader instanceof PBInstanceReader){
			optimisation = ((PBInstanceReader)reader).hasObjectiveFunction();
			if(optimisation){
				problem = new OptToPBSATAdapter(new PseudoOptDecorator((IPBCDCLSolver)solver));
			}
		}


		log("# Started solver " + solver.getClass().getSimpleName());
		log("# on instance " + instancePath);
		log("# Optimisation = " + optimisation);
		log("# Restart strategy = " + telecomStrategy.getRestartStrategy().getClass().getSimpleName());
		log("# Random walk probability = " +randomWalk.getProbability());
		//log("# Number of conflicts before cleaning = " + nbConflicts);


		solveurThread = new Thread() {
			public void run() {
				//Thread thisThread = Thread.currentThread();
				//				if(shouldStop){
				//					System.out.println("coucou");
				//				}
				//				while(!shouldStop){
				try{
					stringWriter = new StringWriter();
					if(problem.isSatisfiable()){
						log("Satisfiable !");
						if(problem instanceof OptToPBSATAdapter){
							log(((OptToPBSATAdapter)problem).getCurrentObjectiveValue()+"");
							reader.decode(((OptToPBSATAdapter)problem).model(new PrintWriter(stringWriter)), new PrintWriter(stringWriter));
						}
						else
							reader.decode(problem.model(),new PrintWriter(stringWriter));
						log(stringWriter.toString());
					}
					else{
						log("Unsatisfiable !");
					}
				} catch (TimeoutException e) {
					log("Timeout, sorry!");      
				}
				//log("Solver has stopped");
				//				}
			}
		};
		solveurThread.start();


		if(isPlotActivated && gnuplotBased){
			traceGnuplot();
		}


	}


	public void initSearchListeners(){
		List<SearchListener> listeners = new ArrayList<SearchListener>();


		if(isPlotActivated)
		{
			if(gnuplotBased){
				if(visuPreferences.isDisplayClausesEvaluation()){
					listeners.add(new LearnedTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-learned")));
				}
				if(visuPreferences.isDisplayClausesSize()){
					listeners.add(new LearnedClausesSizeTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-learned-clauses-size-clean")));
				}
				if(visuPreferences.isDisplayConflictsDecision()){
					listeners.add(new ConflictLevelTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-level-clean")));
				}
				if(visuPreferences.isDisplayConflictsTrail()){
					listeners.add(new ConflictDepthTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-conflict-depth-clean")));
				}

				if(visuPreferences.isDisplayDecisionIndexes()){
					listeners.add(new DecisionTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-pos"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-neg"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-restart"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-decision-indexes-clean")));
				}

				if(visuPreferences.isDisplaySpeed()){
					listeners.add(new SpeedTracing(
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed"),
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed-clean"), 
							new FileBasedVisualizationTool(whereToWriteFiles + "-speed-restart")));
				}
				if(visuPreferences.isDisplayVariablesEvaluation()){
					listeners.add(new HeuristicsTracing(new FileBasedVisualizationTool(whereToWriteFiles + "-heuristics")));
				}
			}

			else if(chartBased){

				if(visu != null){
					visu.setVisible(false);
				}

				visu = new SolverVisualisation(visuPreferences,isPlotActivated);


				visu.setnVar(solver.nVars());
				if(visuPreferences.isDisplayClausesEvaluation()){
					listeners.add(new LearnedTracing(new ChartBasedVisualizationTool(visu.getClausesEvaluationTrace())));
				}
				if(visuPreferences.isDisplayClausesSize()){
					listeners.add(new LearnedClausesSizeTracing(
							new ChartBasedVisualizationTool(visu.getLearnedClausesSizeTrace()),
							new ChartBasedVisualizationTool(visu.getLearnedClausesSizeRestartTrace()),
							new ChartBasedVisualizationTool(visu.getLearnedClausesSizeCleanTrace())
							));
				}
				if(visuPreferences.isDisplayConflictsDecision()){
					listeners.add(new ConflictLevelTracing(
							new ChartBasedVisualizationTool(visu.getConflictLevelTrace()), 
							new ChartBasedVisualizationTool(visu.getConflictLevelRestartTrace()),
							new ChartBasedVisualizationTool(visu.getConflictLevelCleanTrace())));
				}
				if(visuPreferences.isDisplayConflictsTrail()){
					listeners.add(new ConflictDepthTracing(
							new ChartBasedVisualizationTool(visu.getConflictDepthTrace()),
							new ChartBasedVisualizationTool(visu.getConflictDepthRestartTrace()),
							new ChartBasedVisualizationTool(visu.getConflictDepthCleanTrace())));
				}
				if(visuPreferences.isDisplayDecisionIndexes()){
					listeners.add(new DecisionTracing(
							new ChartBasedVisualizationTool(visu.getPositiveDecisionTrace()),
							new ChartBasedVisualizationTool(visu.getNegativeDecisionTrace()),
							new ChartBasedVisualizationTool(new TraceComposite(visu.getRestartPosDecisionTrace(),visu.getRestartNegDecisionTrace())),
							new ChartBasedVisualizationTool(new TraceComposite(visu.getCleanPosDecisionTrace(),visu.getCleanNegDecisionTrace()))));
				}
				if(visuPreferences.isDisplaySpeed()){
					listeners.add(new SpeedTracing(
							new ChartBasedVisualizationTool(visu.getSpeedTrace()), 
							new ChartBasedVisualizationTool(visu.getSpeedCleanTrace()), 
							new ChartBasedVisualizationTool(visu.getSpeedRestartTrace())));
				}
				if(visuPreferences.isDisplayVariablesEvaluation()){
					listeners.add(new HeuristicsTracing(new ChartBasedVisualizationTool(visu.getHeuristicsTrace())));
				}
			}

		}
		listeners.add(this);

		solver.setSearchListener(new MultiTracing(listeners));

	}


	public boolean isGnuplotBased() {
		return gnuplotBased;
	}

	public void setGnuplotBased(boolean gnuplotBased) {
		this.gnuplotBased = gnuplotBased;
	}

	public boolean isChartBased() {
		return chartBased;
	}

	public void setChartBased(boolean chartBased) {
		this.chartBased = chartBased;
	}

	public void hasClickedOnRestart(){
		telecomStrategy.setHasClickedOnRestart(true);
		String choix = (String)listeRestarts.getSelectedItem();

		boolean isNotSameRestart = !choix.equals(currentRestart);
		boolean shouldInit = isNotSameRestart;

		RestartStrategy restart = new NoRestarts();
		SearchParams params = telecomStrategy.getSearchParams();

		if(choix.equals("LubyRestarts")){
			boolean factorChanged = false;
			int factor = LubyRestarts.DEFAULT_LUBY_FACTOR;
			if(factorField.getText()!=null){
				factor = Integer.parseInt(factorField.getText());
			}
			// if the current restart is a LubyRestart
			if(isNotSameRestart){
				restart = new LubyRestarts(factor);
				telecomStrategy.setRestartStrategy(restart);
			}
			else{
				factorChanged = !(factor==((LubyRestarts)telecomStrategy.getRestartStrategy()).getFactor());
			}
			// if the factor has changed
			if(factorChanged){
				restart = telecomStrategy.getRestartStrategy();
				((LubyRestarts)restart).setFactor(factor);
			}
			shouldInit = isNotSameRestart || factorChanged;

			if(shouldInit){
				restart.init(params);
				log("Init restart");
			}

		}

		else try{
			restart = (RestartStrategy)Class.forName(RESTART_PATH+"."+choix).newInstance();
			assert restart!=null;
			telecomStrategy.setRestartStrategy(restart);
			telecomStrategy.init(params);

		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			e.printStackTrace();
		}
		catch(InstantiationException e){
			e.printStackTrace();
		}

		currentRestart = choix;


		//		if(shouldInit)
		//			telecomStrategy.setRestartStrategy(restart,params);

		log("Set " + RESTART + " to "+ choix);
	}

	public void hasClickedOnApplyRW(){
		double proba=0;
		if(probaRWField!=null)
			proba = Double.parseDouble(probaRWField.getText());

		randomWalk.setProbability(proba);
		log("Set probability to " + proba);
	}

	public void hasClickedOnApplyPhase(){
		String phaseName = (String)phaseList.getSelectedItem();
		currentPhaseSelectionStrategy = phaseName;
		IPhaseSelectionStrategy phase = null;
		try{
			phase= (IPhaseSelectionStrategy)Class.forName(PHASE_PATH_SAT+"."+phaseName).newInstance();
			phase.init(solver.nVars()+1);
			telecomStrategy.setPhaseSelectionStrategy(phase);
			log("Told the solver to apply a new phase strategy :" + currentPhaseSelectionStrategy);
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		catch(IllegalAccessException e){
			e.printStackTrace();
		}
		catch(InstantiationException e){
			e.printStackTrace();
		}

	}

	public void hasChangedCleaningValue(){
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);
		log("Changed number of conflicts before cleaning to " + nbConflicts);
		if(activityRadio.isSelected()){
			solver.setLearnedConstraintsDeletionStrategy(telecomStrategy, LearnedConstraintsEvaluationType.ACTIVITY);
			log("Changed clauses evaluation type to activity");
		}
		else if(lbdRadio.isSelected()){
			solver.setLearnedConstraintsDeletionStrategy(telecomStrategy, LearnedConstraintsEvaluationType.LBD);
			log("Changed clauses evaluation type to lbd");
		}
		else if(lbd2Radio.isSelected()){
			solver.setLearnedConstraintsDeletionStrategy(telecomStrategy, LearnedConstraintsEvaluationType.LBD2);
			log("Changed clauses evaluation type to lbd2");
		}
	}

	public void hasClickedOnClean(){
		log("Told the solver to clean");
		telecomStrategy.setHasClickedOnClean(true);
		//log("Has clicked on " + CLEAN);
	}

	public void hasClickedOnUseOriginalStrategy(){
		int nbConflicts = cleanValues[cleanSlider.getValue()];
		telecomStrategy.setNbClausesAtWhichWeShouldClean(nbConflicts);

		telecomStrategy.setUseTelecomStrategyAsLearnedConstraintsDeletionStrategy(true);

		solver.setLearnedConstraintsDeletionStrategy(telecomStrategy,LearnedConstraintsEvaluationType.ACTIVITY);
		activityRadio.setSelected(true);

		log("Solver now cleans clauses every " + cleanValues[cleanSlider.getValue()] + " conflicts and bases evaluation of clauses on activity");

		setCleanPanelOriginalStrategyEnabled(false);
	}


	public void hasClickedOnApplySimplification(){
		if(simplificationSimpleRadio.isSelected()){
			solver.setSimplifier(SimplificationType.SIMPLE_SIMPLIFICATION);
			log("Told the solver to use " + SIMPLIFICATION_SIMPLE);
		}
		else if(simplificationExpensiveRadio.isSelected()){
			solver.setSimplifier(SimplificationType.EXPENSIVE_SIMPLIFICATION);
			log("Told the solver to use " + SIMPLIFICATION_EXPENSIVE);
		}
		else{
			solver.setSimplifier(SimplificationType.NO_SIMPLIFICATION);
			log("Told the solver to use " + SIMPLIFICATION_NO);
		}

	}


	public List<String> getListOfRestartStrategies(){
		List<String> resultRTSI = RTSI.find(RESTART_STRATEGY_CLASS);
		List<String> finalResult = new ArrayList<String>();

		//		finalResult.add(RESTART_NO_STRATEGY);

		for(String s:resultRTSI){
			if(!s.contains("Remote")){
				finalResult.add(s);
			}
		}

		return finalResult;
	}

	public List<String> getListOfPhaseStrategies(){
		List<String> resultRTSI = RTSI.find(PHASE_STRATEGY_CLASS);
		List<String> finalResult = new ArrayList<String>();

		//		finalResult.add(RESTART_NO_STRATEGY);

		for(String s:resultRTSI){
			if(!s.contains("Remote")){
				finalResult.add(s);
			}
		}

		return finalResult;
	}


	public List<String> getListOfSolvers(){
		ASolverFactory factory;

		List<String> result = new ArrayList<String>();

		factory = org.sat4j.minisat.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(MINISAT_PREFIX+"."+s);
		}

		factory = org.sat4j.pb.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(PB_PREFIX+"."+s);
		}

		Collections.sort(result);

		return result;
	}

	public List<String> getListOfPBSolvers(){
		ASolverFactory factory;

		List<String> result = new ArrayList<String>();

		factory = org.sat4j.pb.SolverFactory.instance();
		for(String s:factory.solverNames()){
			result.add(PB_PREFIX+"."+s);
		}

		Collections.sort(result);

		return result;
	}

	public void modifyRestartParamPanel(){
		restartPropertiesPanel.removeAll();
		if(listeRestarts.getSelectedItem().equals("LubyRestarts")){
			restartPropertiesPanel.add(factorLabel);
			restartPropertiesPanel.add(factorField);
		}
		else{
			restartPropertiesPanel.add(noParameterLabel);
		}
		setRestartPropertiesPanelEnabled(true);
		restartPropertiesPanel.repaint();
		restartPanel.repaint();
		restartPanel.paintAll(restartPanel.getGraphics());
		this.repaint();
	}

	public void log(String message){
		logsameline(message+"\n");
	}
	public void logsameline(String message){
		console.append(message);
		console.setCaretPosition(console.getDocument().getLength() );
		console.repaint();
		this.repaint();
	}


	public void openFileChooser(){
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showDialog(this, "Choose instance");
		if(returnVal==JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			instancePath = file.getAbsolutePath();
			instancePathField.setText(instancePath);
			updateListOfSolvers();
		}
	}

	protected Reader createReader(ICDCL theSolver, String problemname) {
		if (theSolver instanceof IPBSolver) {
			return new PBInstanceReader((IPBSolver) theSolver);
		}
		return new InstanceReader(theSolver);
	}

	public void updateListOfSolvers(){

		if(instancePath.endsWith(".opb")){
			listeSolvers = new JComboBox(getListOfPBSolvers().toArray());
			listeSolvers.setSelectedItem("pb.Default");
			selectedSolver = "pb.Default";
		}
		else{
			listeSolvers = new JComboBox(getListOfSolvers().toArray());
			listeSolvers.setSelectedItem("minisat.Default");
			selectedSolver = "minisat.Default";
		}

	}


	public void updateRestartStrategyPanel(){
		listeRestarts.setSelectedItem(currentRestart);

	}

	public void setInstancePanelEnabled(boolean enabled){
		instanceLabel.setEnabled(enabled);
		instancePathField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		instancePanel.repaint();
	}

	public void setChoixSolverPanelEnabled(boolean enabled){
		listeSolvers.setEnabled(enabled);
		choixSolver.setEnabled(enabled);
		solverLineParamLineRadio.setEnabled(enabled);
		solverLineParamRemoteRadio.setEnabled(enabled);
		solverListParamListRadio.setEnabled(enabled);
		solverListParamRemoteRadio.setEnabled(enabled);
		optimisationModeCB.setEnabled(enabled);
		// TODO regarder si le customized solver etait en mode optimisation ou pas
		choixSolverPanel.repaint();
	}

	public void setRestartPanelEnabled(boolean enabled){
		listeRestarts.setEnabled(enabled);
		restartButton.setEnabled(enabled);
		chooseRestartStrategyLabel.setEnabled(enabled);
		setRestartPropertiesPanelEnabled(enabled);
		restartPanel.repaint();
	}

	public void setRestartPropertiesPanelEnabled(boolean enabled){
		for(Component c:restartPropertiesPanel.getComponents()){
			c.setEnabled(enabled);
		}
		restartPropertiesPanel.repaint();
	}

	public void setRWPanelEnabled(boolean enabled){
		probaRWLabel.setEnabled(enabled);
		probaRWField.setEnabled(enabled);
		applyRWButton.setEnabled(enabled);
		rwPanel.repaint();
	}

	public void setCleanPanelEnabled(boolean enabled){
		manualCleanLabel.setEnabled(enabled);
		deleteClauseLabel.setEnabled(enabled);
		cleanSlider.setEnabled(enabled);
		cleanButton.setEnabled(enabled);
		evaluationLabel.setEnabled(enabled);
		activityRadio.setEnabled(enabled);
		lbdRadio.setEnabled(enabled);
		lbd2Radio.setEnabled(enabled);
		cleanAndEvaluationApplyButton.setEnabled(enabled);
		cleanUseOriginalStrategyCB.setEnabled(enabled);
		speedLabel.setEnabled(enabled);
		speedUnitLabel.setEnabled(enabled);
		speedNameLabel.setEnabled(enabled);
		cleanPanel.repaint();
	}

	public void setCleanPanelOriginalStrategyEnabled(boolean enabled){
		cleanUseOriginalStrategyCB.setEnabled(enabled);
		manualCleanLabel.setEnabled(!enabled);
		deleteClauseLabel.setEnabled(!enabled);
		activityRadio.setEnabled(!enabled);
		evaluationLabel.setEnabled(!enabled);
		lbdRadio.setEnabled(!enabled);
		lbd2Radio.setEnabled(!enabled);
		cleanAndEvaluationApplyButton.setEnabled(!enabled);
		cleanSlider.setEnabled(!enabled);
		cleanButton.setEnabled(!enabled);
		cleanPanel.repaint();
	}

	public void setPhasePanelEnabled(boolean enabled){
		phaseList.setEnabled(enabled);
		phaseListLabel.setEnabled(enabled);
		phaseApplyButton.setEnabled(enabled);
		restartPanel.repaint();
	}

	public void setSimplifierPanelEnabled(boolean enabled){
		simplificationNoRadio.setEnabled(enabled);
		simplificationExpensiveRadio.setEnabled(enabled);
		simplificationSimpleRadio.setEnabled(enabled);
		simplificationApplyButton.setEnabled(enabled);
		simplifierPanel.repaint();
	}

	public void setKeepSolverHotPanelEnabled(boolean enabled){
		keepSolverHotCB.setEnabled(enabled);
		applyHotSolver.setEnabled(enabled);
		hotSolverPanel.repaint();
	}


	public void activateGnuplotTracing(boolean b){
		isPlotActivated=b;
		if(solver!=null)
			initSearchListeners();
		if(startStopButton.getText().equals(STOP) && b){
			if(this.gnuplotBased){
				traceGnuplot();
				this.visu.setVisible(false);
			}
			else{
				stopGnuplot();
				this.visu.setVisible(true);
			}
		}
		else if(!b){
			stopGnuplot();
			if(this.visu!=null)
				this.visu.setVisible(false);
		}
	}

	public void traceGnuplot(){

		int nbVariables = solver.nVars();

		if(gnuplotProcess==null)
			try {

				double verybottom=0.0;
				double bottom=0.33;
				double top=0.66;
				double left=0.0;
				double middle=0.33;
				double right=0.66;

				double width=0.33;
				double height=0.33;



				PrintStream out = new PrintStream(new FileOutputStream(instancePath+"-gnuplot.gnuplot"));
				out.println("set terminal x11");
				out.println("set multiplot");
				out.println("set autoscale x");
				out.println("set autoscale y");
				out.println("set nologscale x");
				out.println("set nologscale y");
				out.println("set ytics auto");

				GnuplotFunction f = new GnuplotFunction("2", Color.black, "");

				//bottom right: Decision Level when conflict
				if(visuPreferences.isDisplayConflictsDecision()){
					out.println("set size "+width + "," + height);
					out.println("set origin "+right + "," + bottom);
					out.println("set title \"Decision level at which the conflict occurs\"");
					out.println("set autoscale ymax"); 
					out.println("set y2range [0:]");
					GnuplotDataFile conflictLevelDF = new GnuplotDataFile(instancePath+ "-conflict-level.dat",Color.magenta,"Conflict Level");
					GnuplotDataFile conflictLevelRestartDF = new GnuplotDataFile(instancePath+ "-conflict-level-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile conflictLevelCleanDF = new GnuplotDataFile(instancePath+ "-conflict-level-clean.dat",Color.orange,"Clean","impulses");
					//out.println(gnuplotPreferences.generatePlotLine(conflictLevelDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{conflictLevelDF}, new GnuplotDataFile[]{conflictLevelRestartDF,conflictLevelCleanDF}, true));
//					out.println(visuPreferences.generatePlotLine(conflictLevelDF,f, instancePath+ "-conflict-level-restart.dat", true));
				}

				//top left: size of learned clause
				if(visuPreferences.isDisplayClausesSize()){
					out.println("unset autoscale");
					out.println("set autoscale x");
					out.println("set autoscale ymax"); 
					out.println("set y2range [0:]");
					//					out.println("set y2range[0:"+nbVariables+"]");
					//					out.println("set autoscale y2");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + top);
					out.println("set title \"Size of the clause learned (after minimization if any)\"");
					GnuplotDataFile learnedClausesDF = new GnuplotDataFile(instancePath+ "-learned-clauses-size.dat",Color.blue,"Size");
					GnuplotDataFile learnedClausesRestartDF = new GnuplotDataFile(instancePath+ "-learned-clauses-size-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile learnedClausesCleanDF = new GnuplotDataFile(instancePath+ "-learned-clauses-size-clean.dat",Color.orange,"Clean","impulses");
					//out.println(gnuplotPreferences.generatePlotLine(learnedClausesDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{learnedClausesDF}, new GnuplotDataFile[]{learnedClausesCleanDF,learnedClausesRestartDF}, true));
				}

				//top middle: clause activity
				if(visuPreferences.isDisplayConflictsDecision()){
					out.println("set autoscale x");
					out.println("set autoscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + top);
					out.println("set title \"Value of learned clauses evaluation\"");
					GnuplotDataFile learnedDF = new GnuplotDataFile(instancePath+ "-learned.dat",Color.blue,"Evaluation");
					out.println(visuPreferences.generatePlotLine(learnedDF,f,"", false));
				}

				// for bottom graphs, y range should be O-maxVar
				out.println("set autoscale x");
				out.println("set nologscale x");
				out.println("set nologscale y");
				out.println("set autoscale y");
				out.println("set yrange [1:"+nbVariables+"]");
				out.println("set ytics add (1,"+ nbVariables +")");

				//bottom left: index decision variable
				if(visuPreferences.isDisplayDecisionIndexes()){
					out.println("unset autoscale");
					out.println("if(system(\"head "+ instancePath+ "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;}");
					out.println("if(system(\"head "+ instancePath+ "-decision-indexes-pos.dat | wc -l\")!=0){set yrange [1:"+nbVariables+"]};");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + bottom);
					out.println("set title \"Index of the decision variables\"");
					GnuplotDataFile negativeDF = new GnuplotDataFile(instancePath+ "-decision-indexes-neg.dat", Color.red,"Negative Decision");
					GnuplotDataFile decisionRestartDF = new GnuplotDataFile(instancePath+ "-decision-indexes-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile decisionCleanDF = new GnuplotDataFile(instancePath+ "-decision-indexes-clean.dat",Color.orange,"Clean","impulses");
					//					out.println(gnuplotPreferences.generatePlotLine(negativeDF, true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{negativeDF}, new GnuplotDataFile[]{decisionRestartDF,decisionCleanDF}, true, visuPreferences.getNbLinesRead()*4));
//					out.println(visuPreferences.generatePlotLine(negativeDF,f,instancePath+ "-decision-indexes-restart.dat" , true, visuPreferences.getNbLinesRead()*4));

					//verybottom left: index decision variable
					out.println("unset autoscale");
					out.println("if(system(\"head "+ instancePath+ "-decision-indexes-pos.dat | wc -l\")!=0){set autoscale x;set yrange [1:"+nbVariables+"]; set y2range [0:]; }");
					//					out.println("set autoscale y");

					//					out.println("if(system(\"head "+ instancePath+ "-decision-indexes-pos.dat | wc -l\")!=0){set yrange [1:"+nbVariables+"];}");
					//					out.println("set nologscale x");
					//					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+left + "," + verybottom);
					out.println("set title \"Index of the decision variables\"");
					GnuplotDataFile positiveDF = new GnuplotDataFile(instancePath+ "-decision-indexes-pos.dat", Color.green,"Positive Decision");
					//					out.println(gnuplotPreferences.generatePlotLine(positiveDF, true));
//					out.println(visuPreferences.generatePlotLine(positiveDF,f,instancePath+ "-decision-indexes-restart.dat", true, visuPreferences.getNbLinesRead()*4));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{positiveDF}, new GnuplotDataFile[]{decisionRestartDF,decisionCleanDF}, true, visuPreferences.getNbLinesRead()*4));
				}

				//top right: depth search when conflict
				if(visuPreferences.isDisplayConflictsTrail()){
					out.println("set autoscale x");
					out.println("set autoscale y");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+right + "," + top);
					out.println("set title \"Trail level when the conflict occurs\"");
					out.println("set y2range [0:]");
					GnuplotDataFile trailLevelDF = new GnuplotDataFile(instancePath+ "-conflict-depth.dat", Color.magenta, "Trail level");
					GnuplotFunction nbVar2 = new GnuplotFunction(""+nbVariables/2, Color.green, "#Var/2");
					GnuplotDataFile trailLevelRestartDF = new GnuplotDataFile(instancePath+ "-conflict-depth-restart.dat",Color.gray,"Restart","impulses");
					GnuplotDataFile trailLevelCleanDF = new GnuplotDataFile(instancePath+ "-conflict-depth-clean.dat",Color.orange,"Clean","impulses");
					//					out.println(gnuplotPreferences.generatePlotLine(trailLevelDF,true));
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{trailLevelDF}, new GnuplotDataFile[]{trailLevelRestartDF,trailLevelCleanDF}, new GnuplotFunction[]{nbVar2}, true));
					out.println(visuPreferences.generatePlotLine(trailLevelDF, 
							nbVar2, instancePath+ "-conflict-level-restart.dat",true));
				}

				//bottom middle: variable activity
				if(visuPreferences.isDisplayVariablesEvaluation()){
					out.println("unset autoscale");
					out.println("set autoscale y");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set yrange [1:"+nbVariables+"]");
					out.println("set xrange [0.5:*]");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + bottom);
					out.println("set title \"Value of variables activity\"");
					GnuplotDataFile heuristicsDF = new GnuplotDataFile(instancePath+ "-heuristics.dat",Color.red,"Activity","lines");
					out.println(visuPreferences.generatePlotLine(heuristicsDF, f,"",false));
				}
				//				out.println("plot \"" + instancePath+ "-heuristics.dat\" with lines title \"Activity\"");


				if(visuPreferences.isDisplaySpeed()){
					out.println("set autoscale x");
					out.println("set nologscale x");
					out.println("set nologscale y");
					out.println("set size "+width + "," + height);
					out.println("set origin "+middle + "," + verybottom);
					out.println("set title \"Number of propagations per second\"");
					out.println("set y2range [0:]");
					GnuplotDataFile speedDF = new GnuplotDataFile(instancePath+"-speed.dat",Color.cyan,"Speed","lines");
					GnuplotDataFile cleanDF = new GnuplotDataFile(instancePath +"-speed-clean.dat",Color.orange, "Clean", "impulses");
					GnuplotDataFile restartDF = new GnuplotDataFile(instancePath +"-speed-restart.dat",Color.gray, "Restart", "impulses");
					out.println(visuPreferences.generatePlotLineOnDifferenteAxes(new GnuplotDataFile[]{speedDF}, new GnuplotDataFile[]{cleanDF,restartDF}, true,50));
				}
				out.println("unset multiplot");
				double pauseTime = visuPreferences.getRefreshTime()/1000;
				out.println("pause " + pauseTime);
				out.println("reread");
				out.close();


				log("Gnuplot will start in a few seconds.");



				Thread errorStreamThread = new Thread(){
					public void run(){

						try{
							try {
								Thread.sleep(visuPreferences.getTimeBeforeLaunching());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							gnuplotProcess = Runtime.getRuntime().exec(
									visuPreferences.createCommandLine(instancePath+"-gnuplot.gnuplot"));

							log("Gnuplot should have started now.");

							BufferedReader gnuInt = new BufferedReader(new InputStreamReader(gnuplotProcess.getErrorStream()));
							String s;

							while((s=gnuInt.readLine())!=null){
								if(s.trim().length()>0 && !s.toLowerCase().contains("warning") && !s.toLowerCase().contains("plot"))
									System.out.println(s);
							}
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				};
				errorStreamThread.start();




			} catch (IOException e) { 
				e.printStackTrace();
			}
	}

	public void stopGnuplot(){
		if(gnuplotProcess!=null){
			gnuplotProcess.destroy();
			log("Gnuplot should be deactivated...");
		}
		gnuplotProcess=null;
	}

	public VisuPreferences getGnuplotPreferences() {
		return visuPreferences;
	}

	public void setGnuplotPreferences(VisuPreferences gnuplotPreferences) {
		this.visuPreferences = gnuplotPreferences;
	}

	public DetailedCommandPanel getThis(){
		return this;
	}

	public ISolver getSolver(){
		return (ISolver)problem;
	}

	private long begin, end;
	private int propagationsCounter;
	//	private long index;

	//	private int nVar;
	private int conflictCounter;

	private PrintStream outSolutionFound;

	private void updateWriter() {
		try {
			outSolutionFound = new PrintStream(new FileOutputStream(whereToWriteFiles + "_solutions.dat"));
		} catch (FileNotFoundException e) {
			outSolutionFound = System.out;
		}

	}

	public void init(ISolverService solverService) {
		//		nVar = solverService.nVars();
		conflictCounter=0;
	}

	public void assuming(int p) {
	}

	public void propagating(int p, IConstr reason) {
		end = System.currentTimeMillis();
		if (end - begin >= 2000) {
			long tmp = (end - begin);
			//			index += tmp;
			speedLabel.setText(propagationsCounter / tmp * 1000+"");
			speedLabel.invalidate();

			begin = System.currentTimeMillis();
			propagationsCounter = 0;
		}
		propagationsCounter++;
	}

	public void backtracking(int p) {
	}

	public void adding(int p) {
	}

	public void learn(IConstr c) {
	}

	public void delete(int[] clause) {
	}

	public void conflictFound(IConstr confl, int dlevel, int trailLevel) {
		conflictCounter++;
	}

	public void conflictFound(int p) {
	}

	public void solutionFound(int[] model) {
		//if(problem.)
		log("Found a solution !! ");
		logsameline(stringWriter.toString());
		stringWriter.getBuffer().delete(0, stringWriter.getBuffer().length());
		outSolutionFound.println(conflictCounter + "\t");
	}

	public void beginLoop() {
	}

	public void start() {
	}

	public void end(Lbool result) {
	}

	public void restarting() {
		end = System.currentTimeMillis();
		speedLabel.setText(propagationsCounter / (end - begin)
				* 1000+"");
	}

	public void backjump(int backjumpLevel) {
	}

	public void cleaning() {
		end = System.currentTimeMillis();
		speedLabel.setText(propagationsCounter / (end - begin) * 1000+"");
	}

	public class MyTabbedPane extends JTabbedPane {
		private static final long serialVersionUID = 1L;

		@Override
		public void setSelectedIndex(int index){
			if(this.getTabCount()==5){
				if(index==this.getTabCount()-1){
					//System.out.println("je suis l�");
					if(solver!=null && startStopButton.getText().equals(STOP)){
						String s = solver.toString();
						String res = solver.toString();
						int j=0;
						for(int i=0;i<s.length();i++){
							if(s.charAt(i)!='\n'){
								j++;
							}
							else{
								j=0;
							}
							if(j>80){
								res = new StringBuffer(res).insert(i, '\n').toString();
								j=0;
							}
						}
						textArea.setText(res);
						textArea.setEditable(false);
						textArea.repaint();
						aboutSolverPanel.paint(aboutSolverPanel.getGraphics());
						aboutSolverPanel.repaint();
					}
					else{
						textArea.setText("No solver is running at the moment");
						textArea.repaint();
						textArea.setEditable(false);
						aboutSolverPanel.paint(aboutSolverPanel.getGraphics());
						aboutSolverPanel.repaint();
					}

					//System.out.println(textArea.getText());
				}
			}

			super.setSelectedIndex(index);
		};
	} 

}
