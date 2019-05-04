package gLoad;

import cliente.ClienteSinSeguridad;
import uniandes.gload.core.Task;

public class ClienteServidorSin extends Task {

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

		ClienteSinSeguridad cliente = new ClienteSinSeguridad();
		cliente.ejecutar();
	}	
}
