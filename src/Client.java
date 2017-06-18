


//import dbObjects.model.PlayerScore;
//import dbObjects.model.Question;
import com.firebase.client.Firebase;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
/*
	public DataFirebase() {
		jFirebase = new Firebase("https://quiz-61ac7.firebaseio.com");
		Firebase qs = jFirebase.child("questions");
		ModelPytanie q1 = new ModelPytanie();
		qs.setValue(q1);		
	}	
}*/

public class Client implements Runnable
{
	static private Socket socket;
	static private final int PORT = 2345;
	static private final String SERVER = "localhost";
	
 	private String answer;
	//private Socket socket;
	private BufferedReader wejscie;
	private PrintWriter wyjscie;
	private String nick;
	private int liczbaPytan;
	private int poprawneOdpowiedzi;
 	private ModelPytanie prawOdpowiedz;
	public DataOutputStream output;
    public ObjectInputStream input;
    private Socket s;

    public Client() {}

    public Client(String host, int port)throws IOException {
        s = new Socket(host, port);
        output = new DataOutputStream(s.getOutputStream());
        output.flush();
        input = new ObjectInputStream(s.getInputStream());
        
        wejscie=new BufferedReader(new InputStreamReader(System.in));
    }

    public void run()
    {
        try
        {
            initClient();
            boolean inGame=true;
            // main GameLogic loop
            while(inGame)
            {
                sendRequestToServer(inGame);
                System.out.println("First request send");
                answer = "";
                while (!answer.equalsIgnoreCase("stop"))
                {
                    // send smth to server, for server keep going send question
                    // if sent string 'stop', server will stop send question
                    sendRequestToServer(answer);
                    System.out.println("second request send");
                    //odczytanie odpowiedzi użytkownika
                    prawOdpowiedz = (ModelPytanie) getResponseFromServer();
                    ask(prawOdpowiedz);
                    answer=wejscie.readLine().toUpperCase();
                    //answer=checkAnswerForCommand(answer);
                    checkAnswer(answer, prawOdpowiedz);
                }
                saveScore();
                //printTopTen((ArrayList<PlayerScore>) getResponseFromServer());
                inGame = false;
            }
        }catch (Exception e){
        	System.out.println(e.getMessage());
        }
    }


    public void initClient() throws IOException
    {
        System.out.println("### Please write your nick");
        setClientNick(wejscie.readLine());
        liczbaPytan=0;
        poprawneOdpowiedzi=0;
    }


    public void setClientNick(String nick)
    {

        //this.nick=wejscie.readLine();
        this.nick=nick;
        System.out.println("### Welcome to music quiz "+this.getNick());
        System.out.println("### !!!Quiz starts!!! ###");
        System.out.println("### Console commands: " +
                "\n # stop - stop game"
        );
        System.out.println("####################################");
    }

    private void sendRequestToServer(Object object) throws IOException
    {
        if (object instanceof Boolean)
            output.writeBoolean((Boolean) object);
        if (object instanceof String)
            output.writeUTF((String)object);
    }
    private Object getResponseFromServer() throws IOException, ClassNotFoundException
    {
        Object object = input.readObject();
        return object;
    }
    private void ask(ModelPytanie question)
    {
        System.out.println("### Question #" + (++liczbaPytan));
        System.out.println(question.getPytanie());
    }
    
    
    private void saveScore() throws IOException
    {
        System.out.println("### Your score: " + (int)getScore());
        System.out.println("### Would you like save your score: y/n");
        //String temp = readConsole();
        String temp = wejscie.readLine().toUpperCase();
        if (temp.equalsIgnoreCase("Y"))
        {
            sendRequestToServer(getNick()+"/"+(int)getScore());
        }
        else
        {
            sendRequestToServer("");
        }
    }
    
    private String getNick() {
    	return nick;
    }

    public boolean checkAnswer(String answer, ModelPytanie question) throws IOException
    {
        boolean isAnswerRight=false;

        // check stop command
        if (answer.trim().equalsIgnoreCase("stop")) {
            sendRequestToServer("stop");
            return isAnswerRight;
        }

        if (answer.trim().equalsIgnoreCase(question.getOdpowiedz().trim())) {
            System.out.println("### RIGHT!!!");
            poprawneOdpowiedzi++;
            isAnswerRight=true;
        } else {
            System.out.println("### WRONG!!!");
            isAnswerRight=false;
        }
        return isAnswerRight;
    }


    public double getScore()
    {
        double score=((poprawneOdpowiedzi*liczbaPytan)/Math.pow(liczbaPytan,2))*100;
        return score;
    }



    public static void main(String[] args)
    {
        Client client = null;
        try {
            client = new Client(SERVER,PORT);
        } catch (IOException e) {e.printStackTrace();}

        new Thread(client).start();
    }
}
