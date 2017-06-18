

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class Serwer extends JFrame implements Protocol{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton uruchom, zatrzymaj;
	private JPanel panel;
	private JTextField port;
	private JTextArea komunikaty;
	private int numerPortu = 2345;
	private boolean uruchomiony = false;
	private Vector<QuizObsluga> klienci = new Vector<QuizObsluga>();
	private static Firebase jFirebase = new Firebase("https://quiz-61ac7.firebaseio.com");
	private static Firebase qRef = jFirebase.child("questions");
	private static ModelPytanie mPytanie;
	private static ServerSocket server;
	private static final int PORT = 2345;
	
	public Serwer(){
		super("Quiz");
		setSize(350, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		panel = new JPanel(new FlowLayout());
		komunikaty = new JTextArea();
		komunikaty.setLineWrap(true);
		komunikaty.setEditable(false);
		port = new JTextField((new Integer(numerPortu)).toString(), 8);
		uruchom = new JButton("Uruchom");
		zatrzymaj = new JButton("Zatrzymaj");
		zatrzymaj.setEnabled(false);
		ObslugaZdarzen obsluga = new ObslugaZdarzen();
		uruchom.addActionListener(obsluga);
		zatrzymaj.addActionListener(obsluga);
		panel.add(new JLabel("Port: "));
		panel.add(port);
		panel.add(uruchom);
		panel.add(zatrzymaj);
		add(panel, BorderLayout.NORTH);
		add(new JScrollPane(komunikaty), BorderLayout.CENTER);
		setVisible(true);
	}
	
	private class ObslugaZdarzen implements ActionListener {
		private SerwerWatek srw;

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Uruchom")) {
				srw = new SerwerWatek();
				srw.start();
				uruchomiony = true;
				uruchom.setEnabled(false);
				zatrzymaj.setEnabled(true);
				port.setEnabled(false);
				repaint();
			}
			if (e.getActionCommand().equals("Zatrzymaj")) {
				srw.kill();
				uruchomiony = false;
				zatrzymaj.setEnabled(false);
				uruchom.setEnabled(true);
				port.setEnabled(true);
				repaint();
			}
		}
	}
	
	private class SerwerWatek extends Thread {
		private ServerSocket server;

		public void kill() {
			try {
				server.close();
				/*for (QuizObsluga klient : klienci) {
					try {
						klient.wyjscie.println("Serwer przestał działać!");
						klient.socket.close();
					} catch (IOException e) {
					}
				}*/
				wyswietlKomunikat("Wszystkie Połączenia zostały zakończone.\n");
			} catch (IOException e) {
			}
		}

		public void run() {
			try {
				server = new ServerSocket(new Integer(port.getText()));
				System.out.println("Serwer quizu został uruchomiony na porcie: " + port.getText());
				wyswietlKomunikat("Serwer quizu uruchomiony na porcie: " + port.getText() + "\n");
				while (uruchomiony) {
					Socket socket = server.accept();
					InetAddress addr = socket.getInetAddress();
					System.out.println("Połączenie z adresu: " + addr.getHostName() + " [" + addr.getHostAddress() + "]");
					wyswietlKomunikat("Nowe połączenie.\n");
					//new QuizObsluga(socket).start();
					Runnable r = () -> play(socket);
					new Thread(r).start();
				}
			} catch (SocketException e) {
			} catch (Exception e) {
				wyswietlKomunikat(e.toString());
			} finally {
				try {
					if (server != null)
						server.close();
				} catch (IOException e) {
					wyswietlKomunikat(e.toString());
				}
			}
			wyswietlKomunikat("Serwer quizu zatrzymany.\n");
		}
		
		private void play(Socket client)
	    {
			//InetAddress addr = client.getInetAddress();
			//System.out.println("Połączenie z adresu: " + addr.getHostName() + " [" + addr.getHostAddress() + "]");
			
            try {
            	System.out.println("Trying streaming");
            	
            	//init streams for THIS client
                ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
        		//outputStream.flush();
                DataInputStream input = new DataInputStream(client.getInputStream());
/******************************************/
                File file1 = new File("pytania.txt");
        		File file2 = new File("odpowiedzi.txt");
        		Scanner in1 = new Scanner(file1);
        		Scanner in2 = new Scanner(file2);
        		String zapytanie, odpowiedz;
/*****************************************/
                
                while (input.readBoolean()){
                	System.out.println("Reached first while");
                    while (!input.readUTF().equalsIgnoreCase("stop")){
                    	System.out.println("second while working");
/*****************************************/   	
                    	zapytanie = in1.nextLine();
            			odpowiedz = in2.nextLine();
            			System.out.println(zapytanie + " -> " + odpowiedz);
            			ModelPytanie pytanie = new ModelPytanie(zapytanie, odpowiedz);
            			//ModelPytanie pytanie = new ModelPytanie("Pytanie: Na ile łap spada kot?", "4");
/*****************************************/

                    	
                    	System.out.println(pytanie.pytanie);
                    	outputStream.writeObject(pytanie);
                        //question = getRandomQuestion(listWithQuestions);
                        //sendQuestion(outputStream, question);
                    }
                    //savePlayer(playerScoreDao,input);
                   // sendTopTen(outputStream);
                }
	        }catch (IOException e){}
	    }
		
		private void wyswietlKomunikat(String tekst) {
			komunikaty.append(tekst);
			komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
		}
	}
	
	

	public static void main(String[] args) {
		
		new Serwer();
		
	}
	
	
	private class QuizObsluga extends Thread implements Protocol {
		
		//utoworzenie pola statycznego przechowującego referencje do wszystkich połączeń
		//static Vector<QuizObsluga> klienci = new Vector<QuizObsluga>();
		
		//utworzenie pól przechowujących gniazdo połączenia i jego we/wy, 
		//a także nick, liczbę zadanych pytań oraz liczbę udzielonych poprawnych odpowiedzi
		private Socket socket;
		private BufferedReader wejscie;
		private PrintWriter wyjscie;
		private String nick;
		private int liczbaPytan;
		private int poprawneOdpowiedzi;

		public QuizObsluga(Socket socket) {
			this.socket = socket;
			//InetAddress addr = socket.getInetAddress();
			this.liczbaPytan = 0;
			this.poprawneOdpowiedzi = 0;
			//wyslij(NEW_CONNECTION, addr.getHostAddress());
		}
		
		public void odczytFirebase(){
			Firebase losowyRef = qRef.child("1");
			System.out.println("Uruchomiono odczytFirebase");
			losowyRef.addListenerForSingleValueEvent(new ValueEventListener() {

	            
	            public void onDataChange(DataSnapshot dataSnapshot) {
	                // ...
	                mPytanie = dataSnapshot.getValue(ModelPytanie.class);
	                System.out.println("Pobrano pytanie: " + mPytanie.pytanie);
	                pokazPytanie(mPytanie.pytanie, mPytanie.odpowiedz);
	                
	            }

	            
	            public void onCancelled(FirebaseError databaseError) {
	                // ...
	                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
	            }

	        });
		}
		
		public void wyslij(String protocol, String tekst) {
			wyjscie.println(protocol + tekst);
		}

		private void wyslijDoWszystkich(String tekst) {
			for (QuizObsluga uczestnik : klienci) {
				//synchronizacja dostępu do tablicy połączeń
				synchronized (klienci) {
					//do wszystkich poza samym sobą
					if (uczestnik != this)
						uczestnik.wyjscie.println("<" + nick + "> " + tekst); //odwołanie do ZMIENNEJ - info do każdego (elementu tablicy uczestnicy)
				}
			}
		}

		private void info() {
			wyjscie.print("Witaj " + nick + ", aktualnie grają: ");
			for (QuizObsluga czat : klienci) {
				synchronized (klienci) {
					if (czat != this)
						wyjscie.print(czat.nick + " "); //BRAK odwołania do ZMIENNEJ - info tylko do jednej osoby
				}
			}
			wyjscie.println();
		}
		
		//wyswietla użytkownikowi pytanie
		public void pokazPytanie (String pytanie, String odpowiedz) {
			liczbaPytan++;
			wyjscie.println(pytanie);
			wyjscie.println(odpowiedz);
			String linia;
			try {
				//while (!(linia = wejscie.readLine()).equalsIgnoreCase("/end")) {
					if ((linia=wejscie.readLine()).equalsIgnoreCase(odpowiedz)) {
						wyjscie.println("Gratulacje.");
						poprawneOdpowiedzi++;
						//break;
					} else {
						wyjscie.println("Pudło");
					}
				//}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				System.out.println("finally");
			}
		}
		
		

		public void run() {
			String linia;
			
			//zsynchronizowane dodawanie czatów do tablicy
			synchronized (klienci) {
				klienci.add(this);
			}
			
			//próba obsługi strumienia danych we/wy
			try {
				//utworzenie strumienia danych we/wy
				wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				wyjscie = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
				wyjscie.println("Zostałeś połączony z serwerem. Komenda /end kończy połączenie. Podaj swój nick: ");
				
				nick = wejscie.readLine();
				System.out.println("Do gry dołączył: " + nick);
				
				wyslij(NEW_CONNECTION, nick);
				wyslijDoWszystkich("Pojawił się w grze nowy gracz");
				info();
				
				//odczytanie i przesłanie tesktu
				while (!(linia = wejscie.readLine()).equalsIgnoreCase("/end")) {
					wyslijDoWszystkich("Użytkownik " + nick + "odpowiedział: " + linia);
					
					//ModelPytanie pytanie = new ModelPytanie();
					//pokazPytanie(pytanie.pytanie, pytanie.odpowiedz);
					//pokazPytanie("Pytanie: Na ile łap spada kot?", "4");
					odczytFirebase();
				}
				
				wyslijDoWszystkich("Opuścił grę");
				System.out.println("Grę opuścił: " + nick);
			} catch (IOException e) {
				System.out.println(e);
			
			//ostatecznie zamknięcie we/wy i gniazda
			} finally {
				try {
					wejscie.close();
					wyjscie.close();
					socket.close();
				} catch (IOException e) {
					
				//ostatecznie usunięcie referencji do połączenia
				} finally {
					synchronized (klienci) {
						klienci.removeElement(this);
					}
				}
			}
		}
	}
}
