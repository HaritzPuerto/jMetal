package org.uma.jmetal.algorithm.multiobjective.mombi;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;


@SuppressWarnings("serial")
public abstract class AbstractMOMBI<S extends Solution<?>> extends AbstractGeneticAlgorithm<S,List<S>> {
	private final Problem<S> 				problem;
	private final int 						maxIterations;
	

	private 	  int							 iterations = 0;
	private final SolutionListEvaluator<S> 		 evaluator;
	private final List<Double>					 referencePoint;
	private final List<Double>					 nadirPoint;

	
	/**
	 * Constructor
	 * Creates an instance of the MOMBI algorithm
	 * Needs to provide the following list of parameters
	 * @param <code>problem</code> is the problem to be solved 
	 * @param <code>maxIterations</code> number of maximum iterations the algorithm 
	 * will perform
	 * @param <code>populationSize</code> size of the population used by this algorithm 
	 * @param <code>crossover</code> operator to recombine several solutions
	 * @param <code>mutation</code> operator to alter solutions
	 * @param <code>selection</code> operator to select solutions which will undergo 
	 * recombination or altering processes
	 * @param <code>evaluator</code> mechanism to evaluate the new solutions generated by 
	 */
	public AbstractMOMBI(Problem<S> problem, int maxIterations,
					     CrossoverOperator<S> crossover, MutationOperator<S> mutation, 
					     SelectionOperator<List<S>,S> selection, 
					     SolutionListEvaluator<S> evaluator) {
		super();
		this.problem 			= problem;
		this.maxIterations 		= maxIterations; 
		
		
		// info for the GA
		this.crossoverOperator 	= crossover;
		this.mutationOperator  	= mutation;
		this.selectionOperator  = selection;
		
		// info to evaluate the problem
		this.evaluator = evaluator;
		
		// info required for MOMBI
		this.nadirPoint     = new ArrayList<>(this.getProblem().getNumberOfObjectives());
		this.initializeNadirPoint(this.getProblem().getNumberOfObjectives());
		this.referencePoint = new ArrayList<>(this.getProblem().getNumberOfObjectives());
		this.initializeReferencePoint(this.getProblem().getNumberOfObjectives());
	}
	
	
			
	
	@Override
	protected void initProgress() {
		this.iterations = 1;
	}

	@Override
	protected void updateProgress() {
		this.iterations+=1;
		
	}

	@Override
	protected boolean isStoppingConditionReached() {
		return this.iterations >= this.maxIterations;
	}

	@Override
	protected List<S> createInitialPopulation() {
	    List<S> population = new ArrayList<>(this.getPopulationSize());
	    for (int i = 0; i < this.getPopulationSize(); i++) {
	      S newIndividual = problem.createSolution();
	      population.add(newIndividual);
	    }
	    return population;
	}

	@Override
	protected List<S> evaluatePopulation(List<S> population) {
	    population = evaluator.evaluate(population, problem);

	    return population;
	}

	@Override
	protected List<S> selection(List<S> population) {
	    List<S> matingPopulation = new ArrayList<>(population.size());
	    for (int i = 0; i < this.getPopulationSize(); i++) {
	      S solution = selectionOperator.execute(population);
	      matingPopulation.add(solution);
	    }

	    return matingPopulation;
	}

	@Override
	protected List<S> reproduction(List<S> population) {
	    List<S> offspringPopulation = new ArrayList<>(this.getPopulationSize());
	    for (int i = 0; i < this.getPopulationSize(); i += 2) {
	      List<S> parents = new ArrayList<>(2);
	      parents.add(population.get(i));
	      parents.add(population.get(i + 1));

	      List<S> offspring = crossoverOperator.execute(parents);

	      mutationOperator.execute(offspring.get(0));
	      mutationOperator.execute(offspring.get(1));

	      offspringPopulation.add(offspring.get(0));
	      offspringPopulation.add(offspring.get(1));
	    }
	    return offspringPopulation;
	}

	@Override
	public List<S> getResult() {
	    this.setPopulation(evaluator.evaluate(this.getPopulation(), problem));

	    return this.getPopulation();
	}
	
	@Override
	public void run() {
	    List<S> offspringPopulation;
	    List<S> matingPopulation;

	    this.setPopulation(createInitialPopulation());
	    this.evaluatePopulation(this.getPopulation());
	    initProgress();
	    //specific GA needed computations
	    this.specificMOEAComputations();
	    while (!isStoppingConditionReached()) {
	      matingPopulation = selection(this.getPopulation());
	      offspringPopulation = reproduction(matingPopulation);
	      offspringPopulation = evaluatePopulation(offspringPopulation);
	      this.setPopulation(replacement(this.getPopulation(), offspringPopulation));
	      updateProgress();
	      // specific GA needed computations
	      this.specificMOEAComputations();
	    }		
	}
	
	public abstract void specificMOEAComputations();

	public Problem<S> getProblem() {
		return this.problem;
	}
	
	public List<Double> getReferencePoint() {
		return this.referencePoint;		
	}
	
	public List<Double> getNadirPoint() {
		return this.nadirPoint;
	}
	
	private void initializeReferencePoint(int size) {
		for (int i = 0; i < size; i++)			
			this.getReferencePoint().add(Double.POSITIVE_INFINITY);
	}
	
	private void initializeNadirPoint(int size) {
		
		for (int i = 0; i < size; i++)
			this.getNadirPoint().add(Double.NEGATIVE_INFINITY);
	}
	
	private void updateReferencePoint(S s) {
		for (int i = 0; i < s.getNumberOfObjectives(); i++) 
			this.getReferencePoint().set(i, Math.min(this.getReferencePoint().get(i),s.getObjective(i)));		
	}
	
	private void updateNadirPoint(S s) {
		for (int i = 0; i < s.getNumberOfObjectives(); i++)
			this.getNadirPoint().set(i, Math.max(this.getNadirPoint().get(i),s.getObjective(i)));
	}
	
	public void updateReferencePoint(List<S> population) {		
		for (S solution : population)
			this.updateReferencePoint(solution);
	}
	
	public void updateNadirPoint(List<S> population) {
		for (S solution : population)
			this.updateNadirPoint(solution);
	}

	protected abstract int getPopulationSize();
		
	protected boolean populationIsNotFull(List<S> population) {
		return population.size() < getPopulationSize();
	}
	
}
