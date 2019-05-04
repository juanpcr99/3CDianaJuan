package gLoad;

import cliente.ClienteConSeguridad;
import uniandes.gload.core.Task;

public class ClienteServidorCon extends Task {

	@Override
	public void fail() {
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() {

		ClienteConSeguridad cliente = new ClienteConSeguridad();
		cliente.ejecutar();
	}	
}
