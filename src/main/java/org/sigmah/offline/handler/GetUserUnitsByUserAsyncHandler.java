package org.sigmah.offline.handler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import org.sigmah.client.dispatch.DispatchListener;
import org.sigmah.offline.dao.UserUnitAsyncDAO;
import org.sigmah.offline.dispatch.AsyncCommandHandler;
import org.sigmah.offline.dispatch.OfflineExecutionContext;
import org.sigmah.shared.command.GetUserUnitsByUser;
import org.sigmah.shared.command.GetUsersByOrgUnit;
import org.sigmah.shared.command.result.Authentication;
import org.sigmah.shared.command.result.ListResult;
import org.sigmah.shared.command.result.UserUnitsResult;
import org.sigmah.shared.dto.UserDTO;
import org.sigmah.shared.dto.orgunit.OrgUnitDTO;

public class GetUserUnitsByUserAsyncHandler implements AsyncCommandHandler<GetUserUnitsByUser, UserUnitsResult>,
	DispatchListener<GetUserUnitsByUser, UserUnitsResult> {

	private final UserUnitAsyncDAO userUnitDAO;

	@Inject GetUserUnitsByUserAsyncHandler(UserUnitAsyncDAO userUnitDAO) {
		this.userUnitDAO = userUnitDAO;
	}

	@Override
	public void execute(final GetUserUnitsByUser command, OfflineExecutionContext executionContext, final AsyncCallback<UserUnitsResult> callback) {
		userUnitDAO.get(command.getUserId(), new AsyncCallback<UserUnitsResult>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(UserUnitsResult userUnitsResult) {
				callback.onSuccess(userUnitsResult);
			}
		});
	}

	@Override
	public void onSuccess(GetUserUnitsByUser command, UserUnitsResult result, Authentication authentication) {
		userUnitDAO.saveOrUpdate(result);
	}
}
