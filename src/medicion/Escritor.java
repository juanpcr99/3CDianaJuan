package medicion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import gLoad.GeneradorSin;

public class Escritor {

	private long verificacion;	
	private long respuesta;

	private int perdidas;
	private int tCliente;
	private int tServidor;

	private double cpuLoad;

	private boolean falla;

	public Escritor() {

		verificacion = 0;
		respuesta = 0;

		falla = false;
	}

	public void empVerificacion() {

		verificacion = System.nanoTime();
	}

	public void terVerificacion() {

		verificacion = System.nanoTime() - verificacion;
	}
	
	public void empRespuesta() {

		respuesta = System.nanoTime();
	}

	public void terRespuesta() {
		respuesta = System.nanoTime() - respuesta;
	}

	public void transCliente() {

		tCliente ++;
	}

	public void transServidor() {

		tServidor ++;
	}

	public void transTotales()
	{
		perdidas = Math.abs(tCliente - tServidor);
	}

	public void registrarFallo() {

		falla = true;
	}

	public double getSystemCpuLoad() throws Exception {
		 MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		 ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		 AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		 if (list.isEmpty()) return Double.NaN;
		 Attribute att = (Attribute)list.get(0);
		 Double value = (Double)att.getValue();
		 // usually takes a couple of seconds before we get real values
		 if (value == -1.0) return Double.NaN;
		 // returns a percentage value with 1 decimal point precision
		 cpuLoad = ((int)(value * 1000) / 10.0);
		 return cpuLoad;
	}
	
	public void escribirResultado() {

		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;

		String verifi="";
		String respue="";
		String cpu="";
		String perdi="";
		String estado="";
		String pool="8";
		String carga="400";//400 80		


		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			int iteracion=1;
			while(iteracion<=10)
			{
				archivo = new File ("./docs/PruebaCompleta.txt");
				
				fr = new FileReader (archivo);
				br = new BufferedReader(fr);

				// Lectura del fichero
				String linea;
				int i=0;
				PrintWriter pw = new PrintWriter("./docs/Pool"+pool+"Carga"+carga+"Iter"+iteracion+".txt", "UTF-8");
				pw.println("Pool:Carga:Iteracion:verificacion:respuesta:cpu:perdida:estado");
				System.out.println("Pool:Carga:Iteracion:verificacion:respuesta:cpu:perdida:estado");
				while(i < 5 && (linea=br.readLine())!=null){
					if(i==0){
						verifi = linea.split(":")[1].replaceAll("ns","");
					}else if(i==1){
						respue=linea.split(":")[1].replaceAll("ns","");
					}else if(i==2){
						cpu=linea.split(":")[1].replaceAll("%","");
					}else if(i==3){
						perdi=linea.split(":")[1];
					}else if(i==4){
						estado=linea.split(":")[1];
					}else{
						System.out.println("Algo paso pendejo");
					}
					i++;
					System.out.println("Este es mi i: "+i);
					if(i==5 ){
						i=0;
						pw.println(pool+":"+carga+":"+iteracion+":"+verifi+":"+respue+":"+cpu+":"+perdi+":"+estado);
						System.out.println(pool+":"+carga+":"+iteracion+":"+verifi+":"+respue+":"+cpu+":"+perdi+":"+estado);
					}

				}
				pw.close();
				System.out.println("/////////////////////////Iteracion numero:"+iteracion);
				iteracion++;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta 
			// una excepcion.
			try{                    
				if( null != fr ){   
					fr.close();     
				}                  
			}catch (Exception e2){ 
				e2.printStackTrace();
			}
		}
	}
}