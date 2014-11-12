package factory.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import factory.station.ServiceType;

public class Task implements Serializable {
	
	private static final long serialVersionUID = 5144342921219304672L;
	private final ServiceType action;
	private final Collection<Task> subtasks;
	private boolean finished;

	public Task(ServiceType action, Collection<Task> subtasks) {
		this.action = action;
		this.subtasks = subtasks;
		this.finished = false;
	}

	public EnumSet<ServiceType> getPossibleNextAssemblySteps() {
		final EnumSet<ServiceType> result = EnumSet.noneOf(ServiceType.class);
		for (final Task task : nextSteps(EnumSet.allOf(ServiceType.class))) {
			result.add(task.action);
		}
		return result;
	}
	
	public Task getNextAssemblyStep(ServiceType action) {
		return nextSteps(Arrays.asList(action)).stream().findAny().orElse(null);
	}
	
	private Collection<Task> nextSteps(Collection<ServiceType> possibleActions) {
		if (this.isFinished()) {
			return Collections.emptyList();
		} else {
			final Collection<Task> result = new ArrayList<>();
			for (final Task subtask : subtasks) {
				result.addAll(subtask.nextSteps(possibleActions));
			}
			if (result.isEmpty() && possibleActions.contains(this.action)) {
				result.add(this);
			}
			return result;
		}
	}
	
	public double getProgress() {
		if (isFinished()) {
			return 1;
		} else {
			double progressSum = 0;
			int taskCount = subtasks.size() + 1; // add one for "this".
			for (final Task subtask : subtasks) {
				progressSum += subtask.getProgress();
			}
			return progressSum / taskCount;
		}
	}
	
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFinished() {
		return finished;
	}

}
