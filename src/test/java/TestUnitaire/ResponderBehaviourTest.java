package TestUnitaire;

import fr.ul.miage.agent.AgentChaineHoteliere;
import fr.ul.miage.agent.ResponderBehaviour;
import fr.ul.miage.entite.Room;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ResponderBehaviourTest {
    @Mock
    AgentChaineHoteliere serviceAgent;
    ResponderBehaviour test;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        test = new ResponderBehaviour(serviceAgent);
    }

    @Test
    public void createCombinaisonTest() throws Exception{
        Room r1 = new Room(1,20.0,2,1);
        Room r2 = new Room(2,50.0,5,1);
        Room r3 = new Room(3,40.0,4,1);
        Room r4 = new Room(4,20.0,2,1);
        Room r5 = new Room(5,15.0,1,1);
        Room r6 = new Room(6,10.0,1,1);
        Room r7 = new Room(7,25.0,2,1);
        Room r8 = new Room(8,30.0,3,1);
        Room r9 = new Room(9,20.0,2,1);
        Room r10 = new Room(10,30.0,3,1);
        ArrayList<Room> listRoom = new ArrayList<>(Arrays.asList(r1,r2,r3,r4,r5,r6,r7,r8,r9,r10));
        listRoom.sort((room1, room2) -> room2.getNbBed() - room1.getNbBed());
        System.out.println(listRoom);
        //ResponderBehaviour test = new ResponderBehaviour(serviceAgent);
        ArrayList<Room> previousCombinaison = new ArrayList<>();
        ArrayList<Room> combinaisonReturn = new ArrayList<>();
        combinaisonReturn.add(r2);
        combinaisonReturn.add(r3);
        //when(service.createCombinaison(2,listRoom,previousCombinaison)).thenReturn(combinaisonReturn);
        assertArrayEquals(combinaisonReturn.toArray(),test.createCombinaison(2,listRoom,previousCombinaison).toArray());
        combinaisonReturn.clear();
        previousCombinaison.clear();

        previousCombinaison.add(r2);
        previousCombinaison.add(r3);
        previousCombinaison.add(r10);
        previousCombinaison.add(r1);
        previousCombinaison.add(r4);
        combinaisonReturn.add(r2);
        combinaisonReturn.add(r3);
        combinaisonReturn.add(r10);
        combinaisonReturn.add(r1);
        combinaisonReturn.add(r7);
        assertArrayEquals(combinaisonReturn.toArray(),test.createCombinaison(5,listRoom,previousCombinaison).toArray());
        combinaisonReturn.clear();
        previousCombinaison.clear();

        previousCombinaison.add(r2);
        previousCombinaison.add(r4);
        previousCombinaison.add(r9);
        previousCombinaison.add(r5);
        previousCombinaison.add(r6);
        combinaisonReturn.add(r2);
        combinaisonReturn.add(r7);
        combinaisonReturn.add(r9);
        combinaisonReturn.add(r5);
        combinaisonReturn.add(r6);
        assertArrayEquals(combinaisonReturn.toArray(),test.createCombinaison(5,listRoom,previousCombinaison).toArray());
        combinaisonReturn.clear();
        previousCombinaison.clear();

        previousCombinaison.add(r4);
        previousCombinaison.add(r7);
        previousCombinaison.add(r9);
        previousCombinaison.add(r5);
        previousCombinaison.add(r6);
        assertNull(test.createCombinaison(5,listRoom,previousCombinaison));
    }

}
