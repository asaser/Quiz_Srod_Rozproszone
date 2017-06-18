import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.registry.*;

public class RMIregistryClient {

    private RMIregistryClient() {}
    
    public String addClient() {
        return "Connected to client";
    }

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];	//tutaj chyba zamiast 1 powinnien byæ x bo to mo¿e ile chce graczy wejœæ do gry
        
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            Hello stub = (Hello) registry.lookup("Hi!!!");
            String response = stub.sayHello();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}