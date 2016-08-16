package org.sigmah.server.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.persistence.TypedQuery;
import org.sigmah.server.computation.ServerComputations;
import org.sigmah.server.computation.ServerValueResolver;
import org.sigmah.server.dao.base.EntityManagerProvider;
import org.sigmah.server.domain.Project;
import org.sigmah.server.domain.ProjectModel;
import org.sigmah.server.domain.User;
import org.sigmah.server.domain.element.ComputationElement;
import org.sigmah.shared.computation.Computation;
import org.sigmah.shared.computation.Computations;
import org.sigmah.shared.computation.value.ComputedValue;
import org.sigmah.shared.computation.value.ComputedValues;
import org.sigmah.shared.util.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service handling the update of computations from server-side events.
 * 
 * @author Raphaël Calabro (raphael.calabro@netapsys.fr)
 */
@Singleton
public class ComputationService extends EntityManagerProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ComputationService.class);

	/**
	 * Injection of the service updating the flexible elements values.
	 */
	@Inject
	private ValueService valueService;
	
	/**
	 * Injection of the resolver of values for computation elements.
	 */
	@Inject
	private ServerValueResolver valueResolver;
	
	/**
	 * Search for the project model containing the given computation element.
	 * 
	 * @param computationElement
	 *			Computation element to use for the search.
	 * @return The parent project model or <code>null</code> if the computation
	 * element is not attached to a project model.
	 */
	public ProjectModel getParentProjectModel(final ComputationElement computationElement) {
		
		final TypedQuery<ProjectModel> modelQuery = em().createQuery("SELECT pm From ProjectModel pm WHERE :element = pm.phaseModels.layout.groups.constraints.element OR :element = pm.projectDetails.layout.groups.constraints.element", ProjectModel.class);
		modelQuery.setParameter("element", computationElement);
			
		return modelQuery.getSingleResult();
	}
	
	/**
	 * Update the value of the given computation element for the given project.
	 * 
	 * @param computationElement
	 *			Computation element to update.
	 * @param project
	 *			Project containing the values.
	 * @param user 
	 *			Author of the update.
	 */
	public void updateComputationValueForProject(final ComputationElement computationElement, final Project project, final User user) {
		
		final Computation computation = Computations.parse(computationElement.getRule(), ServerComputations.getAllElementsFromModel(project.getProjectModel()));
		
		final Future<String> computedValue = new Future<>();
		computation.computeValueWithResolver(project.getId(), valueResolver, computedValue.defer());

		try {
			final ComputedValue value = ComputedValues.from(computedValue.getOrThrow());
			valueService.saveValue(value.toString(), computationElement, project.getId(), user);
		} catch (Throwable t) {
			LOGGER.error("An error occured when computing the formula of the element '" + computationElement.getId() + "' for project '" + project.getId() + "'.", t);
		}
	}
}
