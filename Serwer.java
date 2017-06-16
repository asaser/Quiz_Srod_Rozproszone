package Proj1;

import java.io.*;
import java.net.*;
import java.util.*;
import com.firebase.client.Firebase;

class DataFirebase {
	private Firebase jFirebase;
	
	public DataFirebase() {
		jFirebase = new Firebase("https://quiz-61ac7.firebaseio.com");
		Firebase qs = jFirebase.child("questions");
		ModelPytanie q1 = new ModelPytanie();
		qs.setValue(q1);
		
	}
	
	
}

public class Serwer {
	private static ServerSocket server;
	private static final int PORT = 2345;

	public static void main(String[] args) {
		DataFirebase db = new DataFirebase();
		try {
			server = new ServerSocket(PORT);
			System.out.println("Serwer quizu został uruchomiony na porcie: " + PORT);
			
			//oczekiwanie na kolejne połączenia
			while (true) {
				Socket socket = server.accept();
				InetAddress addr = socket.getInetAddress();
				System.out.println("Połączenie z adresu: " + addr.getHostName() + " [" + addr.getHostAddress() + "]");
				new QuizObsluga(socket).start();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

class QuizObsluga extends Thread {
	
	//utoworzenie pola statycznego przechowującego referencje do wszystkich połączeń
	static Vector<QuizObsluga> uczestnicy = new Vector<QuizObsluga>();
	
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
		this.liczbaPytan = 0;
		this.poprawneOdpowiedzi = 0;
	}

	private void wyslijDoWszystkich(String tekst) {
		for (QuizObsluga uczestnik : uczestnicy) {
			//synchronizacja dostępu do tablicy połączeń
			synchronized (uczestnicy) {
				//do wszystkich poza samym sobą
				if (uczestnik != this)
					uczestnik.wyjscie.println("<" + nick + "> " + tekst); //odwołanie do ZMIENNEJ - info do każdego (elementu tablicy uczestnicy)
			}
		}
	}

	private void info() {
		wyjscie.print("Witaj " + nick + ", aktualnie grają: ");
		for (QuizObsluga czat : uczestnicy) {
			synchronized (uczestnicy) {
				if (czat != this)
					wyjscie.print(czat.nick + " "); //BRAK odwołania do ZMIENNEJ - info tylko do jednej osoby
			}
		}
		wyjscie.println();
	}
	
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
		synchronized (uczestnicy) {
			uczestnicy.add(this);
		}
		
		//próba obsługi strumienia danych we/wy
		try {
			//utworzenie strumienia danych we/wy
			wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			wyjscie = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			wyjscie.println("Zostałeś połączony z serwerem. Komenda /end kończy połączenie. Podaj swój nick: ");
			
			nick = wejscie.readLine();
			System.out.println("Do gry dołączył: " + nick);
			
			wyslijDoWszystkich("Pojawił się w grze nowy gracz");
			info();
			
			//odczytanie i przesłanie tesktu
			while (!(linia = wejscie.readLine()).equalsIgnoreCase("/end")) {
				wyslijDoWszystkich("Użytkownik " + nick + "odpowiedział: " + linia);
				ModelPytanie pytanie = new ModelPytanie();
				pokazPytanie(pytanie.pytanie, pytanie.odpowiedz);
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
				synchronized (uczestnicy) {
					uczestnicy.removeElement(this);
				}
			}
		}
	}
}

































/*
import java.io.*;
import java.net.*;
import java.util.*;


public class Serwer {
	private static ServerSocket server;
	private static final int PORT = 2345;
	private static ArrayList<Socket> listOfClients;

	public static void main(String[] args) {
		try {
			server = new ServerSocket(PORT);
			System.out.println("Serwer quizu został uruchomiony na porcie: " + PORT);
			
			//int liczbaPolaczen = 0;
			//oczekiwanie na kolejne połączenia
			while (true) {
				Socket socket = server.accept();
				//listOfClients.add(socket);
                //System.out.println("\n### New client "+listOfClients.size());
                // replaced with lambda
                //Runnable r = () -> play(socket);
                //new Thread(r).start();
				
				InetAddress addr = socket.getInetAddress();
				System.out.println("Nastąpiło połączenie z adresu: " + addr.getHostName() + " [" + addr.getHostAddress() + "]");
				new QuizObsluga(socket).start();
				//liczbaPolaczen++;
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
}

class QuizObsluga extends Thread {
	
	//utoworzenie pola statycznego przechowującego referencje do wszystkich połączeń
	static Vector<QuizObsluga> czaty = new Vector<QuizObsluga>();
	
	//utworzenie pól przechowujących gniazdo połączenia i jego we/wy, a także nick
	private Socket socket;
	private BufferedReader wejscie;
	private PrintWriter wyjscie;
	private String nick;

	public QuizObsluga(Socket socket) {
		this.socket = socket;
	}

	private void wyslijDoWszystkich(String tekst) {
		for (QuizObsluga czat : czaty) {
			//synchronizacja dostępu do tablicy połączeń
			synchronized (czaty) {
				//do wszystkich poza samym sobą
				if (czat != this)
					czat.wyjscie.println("<" + nick + "> " + tekst); //odwołanie do ZMIENNEJ - informacja do każdego czatu (z pętli for)
			}
		}
	}

	private void info() {
		wyjscie.print("Witaj " + nick + ", aktualnie czatują: ");
		for (QuizObsluga czat : czaty) {
			synchronized (czaty) {
				if (czat != this)
					wyjscie.print(czat.nick + " "); //BRAK odwołania do ZMIENNEJ - informacja tylko do mnie
			}
		}
		wyjscie.println();
	}

	public void run() {
		String linia;
		play(socket);
		
		//zsynchronizowane dodawanie czatów do tablicy
		synchronized (czaty) {
			czaty.add(this);
		}
		
		//próba obsługi strumienia danych we/wy
		try {
			//utworzenie strumienia danych we/wy
			wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			wyjscie = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			wyjscie.println("Połączony z serwerem. Komenda /end kończy połączenie.");
			
			wyjscie.print("Podaj swój nick: ");
			nick = wejscie.readLine();
			System.out.println("Do gry dołączył: " + nick);
			
			wyslijDoWszystkich("Pojawił się w grze");
			info();
			
			//odczytanie i przesłanie tesktu
			while (!(linia = wejscie.readLine()).equalsIgnoreCase("/end")) {
				sendQuestion(wyjscie, "Pytanie: Ile odnóży ma pająk?");
				//wyslijDoWszystkich(linia);
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
				synchronized (czaty) {
					czaty.removeElement(this);
				}
			}
		}
	
	}
	
	private void play(Socket client)
    {
        try (
                //utworzenie strumieni we/wy
                ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
                DataInputStream input = new DataInputStream(client.getInputStream())
            )
        {
                while (input.readBoolean())
                {
                    while (!input.readUTF().equalsIgnoreCase("stop")){
                        //question = getRandomQuestion(listWithQuestions);
                        //sendQuestion(outputStream, "Pytanie: Ile odnóży ma pająk?");
                    }
                    //savePlayer(playerScoreDao,input);
                    //sendTopTen(outputStream);
                }
        }catch (IOException e){}
    }
	

	private void sendQuestion(PrintWriter out, String q) throws IOException
    {
        out.println(q);
        // server log
//        System.out.println("\n#####\nQuestion ID#"+q.getId()+" has been send ");
//        System.out.println("#TEXT: "+q.getQuestionText());
//        System.out.println("#ANSWER: "+q.getAnswer());
//        System.out.println("#####");
    }
	
}
*/