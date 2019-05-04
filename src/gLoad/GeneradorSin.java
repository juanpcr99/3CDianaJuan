package gLoad;

import java.util.concurrent.atomic.AtomicInteger;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorSin {

	private LoadGenerator generator;
	public static AtomicInteger atInt;

	public GeneradorSin() {

		atInt = new AtomicInteger();
		Task tarea = crearTarea();

		int numeroTareas = 80; //400, 200, 80
		int brechaEntreTareas = 100; //20, 40, 100 millis

		generator = new LoadGenerator("Prueba de carga Cliente - Servidor", numeroTareas, tarea, brechaEntreTareas);
		generator.generate();
	}

	public Task crearTarea() {
		return new ClienteServidorSin();
	}

	public static void main (String[] args) {

		new GeneradorSin();
	}
}
