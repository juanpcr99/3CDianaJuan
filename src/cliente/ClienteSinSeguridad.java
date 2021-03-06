package cliente;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import medicion.Escritor;
import seguridad.Certificado;

public class ClienteSinSeguridad {

	//-----------------------------------------------------
	// Constantes protocolo
	//-----------------------------------------------------
	public final static String HOLA = "HOLA";
	public final static String OK = "OK";
	public final static String ALGS = "AES";
	public final static String ALGA = "RSA";
	public final static String ALGHMAC = "HMACSHA1";
	public final static String ERROR = "ERROR";

	private static SecretKey llaveSimetrica;

	private final String IP = "localhost";
	private Certificado certificado;
	private X509Certificate certificadoServidor;
	private Escritor medidor;

	public void ejecutar() {

		certificado = new Certificado();
		medidor = new Escritor();
		try{
			Socket socket = null;
			PrintWriter escritor = null;
			BufferedReader lector = null;

			try	{
				socket = new Socket(IP, 8086);
				escritor = new PrintWriter(socket.getOutputStream(), true);
				lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));		
			}
			catch (Exception e) {
				System.err.println("Exception: " + e.getMessage());
				System.exit(1);
			}

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			try{
				comenzar(lector, escritor);			
			}
			catch (Exception e){
				e.printStackTrace();
			}
			finally {
				System.out.println("Conexion terminada");
				stdIn.close();
				escritor.close();
				lector.close();		
				// cierre el socket y la entrada estándar
				socket.close();
			}	
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	

	public void comenzar( BufferedReader pLector, PrintWriter pEscritor) throws Exception
	{
		String inputLine, outputLine;
		String certificadoString = "";
		int estado = 0;

		pEscritor.println(HOLA);
		System.out.println("Cliente: " + HOLA);
		medidor.transCliente();

		boolean finalizo = false;

		while (!finalizo) 
		{
			switch( estado ) {
			case 0:
				medidor.getSystemCpuLoad();
				inputLine = pLector.readLine();
				if(inputLine != null) {
					System.out.println("Servidor: " + inputLine);

					if (inputLine.equalsIgnoreCase(OK)) 
					{
						medidor.transServidor();
						outputLine = "ALGORITMOS:"+ALGS+":"+ALGA+":"+ALGHMAC;
						estado++;
					} 
					else 
					{
						outputLine = ERROR;
						estado = -1;
					}

					pEscritor.println(outputLine);
					System.out.println("Cliente: " + outputLine);
				}
				break;

			case 1:
				medidor.transCliente();
				inputLine = pLector.readLine();
				if(inputLine != null) {
					System.out.println("Servidor: " + inputLine);

					if(inputLine.equalsIgnoreCase(OK))
					{
						medidor.transServidor();
						byte[] bytes = certificado.createBytes(new Date(), new Date(), ALGA, 512, "SHA1withRSA");
						certificadoString = toByteArrayHexa(bytes);

						pEscritor.println(certificadoString);
						System.out.println("Cliente: Certificado del cliente");	
						estado++;

					}
					else
					{
						estado = -1;
					}
				}
				break;
			case 2:
				medidor.transCliente();
				inputLine = pLector.readLine();
				if(inputLine != null) {
					String sCertificadoServidor = inputLine;
					byte[] certificadoBytes = new byte['�'];
					certificadoBytes = toByteArray(sCertificadoServidor);
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					InputStream in = new ByteArrayInputStream(certificadoBytes);
					certificadoServidor =  (X509Certificate) cf.generateCertificate(in);
					System.out.println("Servidor: Certificado del servidor");
					medidor.transServidor();

					medidor.empVerificacion();
					llaveSimetrica = generateSecretKey();
					byte[] llaveBytes = llaveSimetrica.getEncoded();
					certificado.setLlaveSimetrica(llaveBytes);

					String llaveSim = toByteArrayHexa(llaveBytes);

					outputLine = llaveSim;
					pEscritor.println(outputLine);
					System.out.println("Cliente: "+outputLine); 
					estado++;

				}
				break;

			case 3:
				medidor.transCliente();
				inputLine = pLector.readLine();

				if(inputLine != null){
					System.out.println("Servidor: " + inputLine);
					medidor.transServidor();
				}
				outputLine = OK;
				pEscritor.println(outputLine);
				System.out.println("Cliente: "+ outputLine);

				medidor.terVerificacion();
				//Cifrar datos	
				String sDatos = new String("15;41 24.2028,2 10.4418");

				outputLine = sDatos;
				medidor.empRespuesta();
				pEscritor.println(outputLine);
				pEscritor.println(outputLine);

				System.out.println("Cliente(Datos1): " + outputLine);
				System.out.println("Cliente(Datos2): " + outputLine);

				medidor.transCliente();
				estado++;

				if((inputLine = pLector.readLine()) != null){
					System.out.println("Servidor: "+inputLine);
					medidor.terRespuesta();
					medidor.transServidor();
				}
				finalizo=true;

				break;
			default:
				if(finalizo != true){
					medidor.registrarFallo();
				}
				estado = -1;
				break;
			}
		}		
		medidor.escribirResultado();
	}

	private static byte[] toByteArray(String cert) {
		return DatatypeConverter.parseHexBinary(cert);
	}

	private static String toByteArrayHexa(byte[] byteArray) {

		String out = "";
		for (int i = 0; i < byteArray.length; i++) {
			if ((byteArray[i] & 0xff) <= 0xf) {
				out += "0";
			}
			out += Integer.toHexString(byteArray[i] & 0xff).toUpperCase();
		}

		return out;
	}


	public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGS);
		return keyGenerator.generateKey();
	}

}
