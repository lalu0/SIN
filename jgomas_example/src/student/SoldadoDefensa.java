package student;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import es.upv.dsic.gti_ia.jgomas.*;

public class SoldadoDefensa extends CSoldier{
	private static final long serialVersionUID = 1L;

	
	private Vector3D[] vVolverPath=new Vector3D [1000];
	private boolean bHeVuelto=false;
	private boolean bIsVigia=false;
	
	private int iAmmoThreshold = 50;
	private int iHealthThreshold = 50;
	
	private Vector<AID> m_AidListaMensajeros; //Lista de los aliados que desean recibir mis mensajes

	protected void setup() {		
		AddServiceType("Mensajero");//Me añado como mensajero para que me envien mensajes

		super.setup();
		SetUpPriorities();

		m_AidListaMensajeros = new Vector<AID>();
		buscarMensajeros();

		addBehaviour(new CyclicBehaviour(){//Ver mensajes recibidos
			public void action(){
				MessageTemplate template = MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchConversationId("MS"));
				ACLMessage msg = receive(template);
				if(msg != null){
					AID owner =msg.getSender();
					mensajeRecibido(msg);
				}
			}
		});		
		
		//Aumentos del disparo, cada 10 milisegundos dispara 2 veces si puede
				SetUpPriorities();
				addBehaviour(new TickerBehaviour(this,100){
					public void onTick(){
						if(GetAgentToAim()){					
							Shot(2);
						}
					}			
				});
	}
	
	/* (non-Javadoc)
	 * @see es.upv.dsic.gti_ia.jgomas.CTroop#takeDown()
	 * Este método se invoca antes de morir, si llevo la bandera aviso
	 */
	protected void takeDown(){
		if(this.m_bObjectiveCarried){
			//EnviarMensaje con la posicion
			enviarMensaje("Bandera "+ m_Movement.getPosition());
		}
	}

	/**
     * Este método realiza el envío de mensajes en forma de String
     */
    void enviarMensaje(String mensaje){
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		for (int i = 0;i<m_AidListaMensajeros.size();i++){
			msg.addReceiver(m_AidListaMensajeros.elementAt(i));
		}
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setConversationId("MS");
		msg.setContent(mensaje);
		send(msg);
		System.out.println(getLocalName()+ ": Ha enviado un mensaje: "+mensaje);  		
	}
	/**
	 * Este método implementa la búsqueda de mensajeros, para luego comunicarme con ellos (en principio todo el equipo)
	 * Estos mensajes serán para protocolos como el aviso de que he muerto con la bandera o de que tengo la bandera
	 *  y hay que cambiar la estrategia
	 */
	void buscarMensajeros(){
		try {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Mensajero_Axis");
			dfd.addServices(sd);
			DFAgentDescription[] result = DFService.search(this, dfd);
			if ( result.length > 0 ) {
				for ( int i = 0; i < result.length; i++ ) {
					DFAgentDescription dfdMensajero = result[i];
					AID mensajero = dfdMensajero.getName();
					if ( ! mensajero.equals(getName()) )
						m_AidListaMensajeros.add(mensajero);					
				}
			} 
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	/**
	 * En este método cada agente implementará el tratamiento adecuado de cada mensaje según el tema, el agente que lo envía y el contenido
	 */
	void mensajeRecibido(ACLMessage msg){
		
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods to overload inherited from CTroop class
	//

	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Request for medicine. 
	 * 
	 * This method sends a <b> FIPA REQUEST </b> message to all agents who offers the <tt> m_sMedicService </tt> service.
	 * 
	 * The content of message is: <tt> ( x , y , z ) ( health ) </tt>.
	 * 
	 * Variable <tt> m_iMedicsCount </tt> is updated.
	 * 
	 * <em> It's very useful to overload this method. </em>
	 * 
	 */

	protected void CallForMedic() {

		try {

			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(m_sMedicService);
			dfd.addServices(sd);
			DFAgentDescription[] result = DFService.search(this, dfd);

			if ( result.length > 0 ) {

				m_iMedicsCount = result.length;

				// Fill the REQUEST message
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

				for ( int i = 0; i < result.length; i++ ) {

					DFAgentDescription dfdMedic = result[i];
					AID Medic = dfdMedic.getName();
					if ( ! Medic.equals(getName()) )
						msg.addReceiver(dfdMedic.getName());
					else
						m_iMedicsCount--;
				}
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				msg.setConversationId("CFM");
				msg.setContent(" ( " + m_Movement.getPosition().x + " , " + m_Movement.getPosition().y + " , " + m_Movement.getPosition().z + " ) ( " + GetHealth() + " ) ");
				send(msg);
				System.out.println(getLocalName()+ ": Need a Medic! (v21)");  			

			} else {
				m_iMedicsCount = 0;
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Request for ammunition. 
	 * 
	 * This method sends a <b> FIPA REQUEST </b> message to all agents who offers the <tt> m_sAmmoService </tt> service.
	 * 
	 * The content of message is: <tt> ( x , y , z ) ( ammo ) </tt>.
	 * 
	 * Variable <tt> m_iFieldOpsCount </tt> is updated.
	 * 
	 * <em> It's very useful to overload this method. </em>
	 *    
	 */
	protected void CallForAmmo() {

		super.CallForAmmo();

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////


	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Request for backup. 
	 * 
	 * This method sends a <b> FIPA REQUEST </b> message to all agents who offers the <tt> m_sBackupService</tt> service.
	 * 
	 * The content of message is: <tt> ( x , y , z ) ( SoldiersCount ) </tt>.
	 * 
	 * Variable <tt> m_iSoldiersCount </tt> is updated.
	 * 
	 * <em> It's very useful to overload this method. </em>
	 *    
	 */
	protected void CallForBackup() {


		super.CallForBackup();

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////



	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Update priority of all 'prepared (to execute)' tasks. 
	 * 
	 * This method is invoked in the state <em>STANDING</em>, and it's used to re-calculate the priority of all tasks (targets) int the task list
	 * of the agent. The reason is because JGOMAS Kernel always execute the maximum priority task. 
	 * 
	 * <em> It's very useful to overload this method. </em>
	 *    
	 */
	protected void UpdateTargets() {} 
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Should we update now all 'prepared (to execute)' tasks? 
	 * 
	 * This method is a decision function invoked in the state <em>GOTO_TARGET</em>. A value of <tt> TRUE</tt> break out the inner loop, 
	 * making possible to JGOMAS Kernel extract a more priority task, or update some attributes of the current task.
	 * By default, the return value is <tt> FALSE</tt>, so we execute the current task until it finalizes.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 * @return <tt> FALSE</tt> 
	 * 
	 */
	protected boolean ShouldUpdateTargets() { return true; }  
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The agent has got the objective pack. 
	 * 
	 * This method is called when this agent walks on the objective pack, getting it.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void ObjectivePackTaken() {
		//EnviarMensaje con la posicion
		enviarMensaje("Cojo la bandera "+ m_Movement.getPosition());	
	} // Should we do anything when we take the objective pack? 
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Definition of priorities for each kind of task. 
	 * 
	 * This method can be implemented in CTroop's derived classes to define the task's priorities in agreement to
	 * the role of the new class. Priorities must be defined in the array <tt> m_TaskPriority</tt>. 
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void SetUpPriorities() {

		m_TaskPriority[CTask.TASK_NONE] = 0;
		m_TaskPriority[CTask.TASK_GIVE_MEDICPAKS] = 2000;
		m_TaskPriority[CTask.TASK_GIVE_AMMOPACKS] = 0;
		m_TaskPriority[CTask.TASK_GIVE_BACKUP] = 0;
		m_TaskPriority[CTask.TASK_GET_OBJECTIVE] = 1000;
		m_TaskPriority[CTask.TASK_ATTACK] = 1000;
		m_TaskPriority[CTask.TASK_RUN_AWAY] = 1500;
		m_TaskPriority[CTask.TASK_GOTO_POSITION] = 750;
		m_TaskPriority[CTask.TASK_PATROLLING] = 500;
		m_TaskPriority[CTask.TASK_WALKING_PATH] = 750;

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Action to do if this agent cannot shoot. 
	 * 
	 * This method is called when the agent try to shoot, but has no ammo. The agent will spit enemies out. :-) 
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void PerformNoAmmoAction() {}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Calculates a new destiny position to escape. 
	 * 
	 * This method is called before the agent creates a task for escaping. It generates a valid random point in a radius of 50 units.
	 * Once position is calculated, agent updates its destiny to the new position, and automatically calculates the new direction.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void GenerateEscapePosition() {

		while (true) {
			m_Movement.CalculateNewDestination(50, 50);
			if ( CheckStaticPosition(m_Movement.getDestination().x, m_Movement.getDestination().z) == true ) {
				m_Movement.CalculateNewOrientation();
				return;
			}
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * Algoritmo graph search.
	 */

	
	/**
	 * Calculates a new destiny position to walk. 
	 * 
	 * This method is called before the agent creates a <tt> TASK_GOTO_POSITION</tt> task. It will try (for 5 attempts) to generate a
	 * valid random point in a radius of 20 units. If it doesn't generate a valid position in this cycle, it will try it in next cycle. 
	 * Once a position is calculated, agent updates its destination to the new position, and automatically calculates the new direction.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 * @return <tt> TRUE</tt>: valid position generated / <tt> FALSE</tt> cannot generate a valid position
	 * 
	 */

	
		protected boolean GeneratePath() 
		{

			
			bHeVuelto=false;
			m_iAStarPathIndex=0;//inicilaizamos a 0
			
			System.out.println("La posicion inicial por getposition es "+m_Movement.getPosition().x+"y z es "+m_Movement.getPosition().z);
			
			Vector3D vNewDestination=new Vector3D(m_Movement.getPosition().x,0.0,m_Movement.getPosition().z);
			
			ArrayList<Vector3D> lv3D=new ArrayList<Vector3D>();//para ir metiendo los puntos que luego le pasaré en orden descendente a m_AStarPath
			ArrayList<Nodo> lAbiertos=new ArrayList<Nodo>();
			ArrayList<Nodo> lCerrados=new ArrayList<Nodo>();
			ArrayList<Nodo> lvecinos=new ArrayList<Nodo>();
			
			int xInit =(int) Math.floor(m_Movement.getPosition().x);
			xInit /= 8;
			int zInit = (int) Math.floor(m_Movement.getPosition().z);
			zInit /= 8;
			int xFinal =(int) Math.floor(m_Movement.getDestination().x);
			xFinal /= 8;
			int zFinal = (int) Math.floor(m_Movement.getDestination().z);
			zFinal /= 8;
			
			Nodo nodoInit= new Nodo(xInit,zInit,null);
			Nodo nodoFinal=new Nodo(xFinal,zFinal,null);
			Nodo nodoActual=null;
			 lAbiertos.add(nodoInit);// añado el nodo inicial a la lista de abiertos
			 boolean esNodoFinal=false;
			 while(!esNodoFinal)    //(!lAbiertos.isEmpty()){
			 {
				nodoActual=this.obtenerNodoMejorCoste(lAbiertos,nodoFinal); //obtener el nodo abierto de menor coste
				for(int i=0;i<lAbiertos.size();i++)
				{
					if(nodoActual.comparaEsNodo(lAbiertos.get(i)) )
					{
						lAbiertos.remove(i);
					}
				}
				//System.out.println("2 Labiertos tiene"+lAbiertos.size());
				//lAbiertos.remove(nodoActual);
				lCerrados.add(nodoActual);
				if(nodoActual.comparaEsNodo(nodoFinal))
				{
					esNodoFinal=true;
					break;
					
				}
				lvecinos.clear();
			   lvecinos= this.calcularVecinos(lvecinos,nodoActual);
			   
			   lAbiertos= this.actualizarAbiertos(lAbiertos,lCerrados,lvecinos,nodoFinal);	
			    //System.out.println("Ha entrado en el bucle");
			    //System.out.println("Nodo Actual x:"+nodoActual.getPosX()+" y posicion z: "+nodoActual.getPosZ());
			 }
			 System.out.println("Ha salido");
			 boolean esInicial=false;
			 for(int h=0;h<lCerrados.size();h++)
			 {
				 if(nodoFinal.comparaEsNodo(lCerrados.get(h)))
				 {
					 nodoFinal=lCerrados.get(h);
				 }
			 }
			 Nodo nCurrent=nodoFinal;
			 Nodo nPadre=null;
			 while(!esInicial)//meto las posiciones de los nodos en un array de Vector3D
			 {
				 if(nCurrent.getPadre()==null) esInicial=true;
				 lv3D.add(new Vector3D(nCurrent.getPosX()*8,0,nCurrent.getPosZ()*8));
				 nCurrent=nCurrent.getPadre();		 
			 }
			 Vector3D[] v3D=new Vector3D[lv3D.size()];
			 Vector3D[] v3DReturn=new Vector3D[1000];
			 for(int j=lv3D.size()-1;j>=0;j--)
			 {
				 //for(int h=0;h<lv3D.size();h++)
				 //{
					 
				 v3D[(lv3D.size()-1)-j]=new Vector3D(lv3D.get(j).x,0.0,lv3D.get(j).z);
				// v3DReturn[j]=new Vector3D(lv3D.get(j).x,0.0,lv3D.get(j).z);
				 
				// }
			 }
			// System.out.println(m_Movement.getPosition().x);
			 //System.out.println(m_Movement.getPosition().z);
			 double xInicial=lv3D.get(0).x;
			 double zInicial=lv3D.get(0).z;
			 
			 double xINI=v3D[0].x;
			 double zINI=v3D[0].z;
			 //this.setvVolverPath(v3DReturn);//Me guardo el vector para volver
			 
			 m_AStarPath=v3D;
			 if (CheckStaticPosition(m_AStarPath[0].x,m_AStarPath[0].z) == true)
			 {
			 String posInit = " ( " + m_AStarPath[0].x + " , 0.0 , " + m_AStarPath[0].z + " ) ";
			 AddTask(CTask.TASK_WALKING_PATH, getAID(), posInit, 10000);
			 
			 m_Movement.setDestination(vNewDestination);
			 System.out.println("la posicion inicial es "+vNewDestination.x +" y la z "+vNewDestination.z);
			 System.out.println("El destino actual es "+m_Movement.getDestination().x+" y la z "+m_Movement.getDestination().z);
			 
			 return true;
			 }
			 return false;
			
		}
		
		protected void setvVolverPath(Vector3D[] v)
		{
			this.vVolverPath=v;
		}
		protected Vector3D[] getvVolverPath()
		{
			return this.vVolverPath;
		}

		protected Nodo obtenerNodoMejorCoste(ArrayList<Nodo> lAbiertos,Nodo nFinal)
		{
			Nodo nMejor=null;
			int mejorCoste=10000;
			int costeNodo;
			for(int i=0;i<lAbiertos.size();i++)
			{
				costeNodo=lAbiertos.get(i).CalcularCosteF(nFinal);
			if(costeNodo<mejorCoste)
			{
				mejorCoste=costeNodo;
				nMejor=lAbiertos.get(i);}	
			}
			return nMejor;
		}
		
		protected ArrayList<Nodo> actualizarAbiertos(ArrayList<Nodo> lAbiertos,ArrayList<Nodo> lCerrados,ArrayList<Nodo> lvecinos, Nodo nFinal )
		{
			Nodo nVecino=null;
			//ArrayList<Nodo> auxLAbiertos=lAbiertos;
			boolean estaEnAbiertos=false;
			boolean estaEnCerrados=false;
			for(int i=0;i<lvecinos.size();i++)
			{
				estaEnAbiertos=false;
				estaEnCerrados=false;
				nVecino=lvecinos.get(i);
				for(int k=0;k<lCerrados.size();k++)
				{
					if(nVecino.comparaEsNodo(lCerrados.get(k)))
					{
						estaEnCerrados=true;
					}
				}
				
				if(!estaEnCerrados)
				{
				for(int j=0;j<lAbiertos.size();j++)
				{
					if(nVecino.comparaEsNodo(lAbiertos.get(j)))
					{
						estaEnAbiertos=true;
						if(nVecino.getCosteF()<lAbiertos.get(j).getCosteF())
						{ 
							lAbiertos.remove(j);
							lAbiertos.add(nVecino);
						}
					}
				}
				
				if(!estaEnAbiertos) lAbiertos.add(nVecino);
				}
			}
			
			return lAbiertos;
			
		}
		
		protected ArrayList<Nodo> calcularVecinos(ArrayList<Nodo> lvecinos,Nodo nActual) {
			
			int x=nActual.getPosX();
			int z=nActual.getPosZ();
			
			if ( m_Map.CanWalk(x+1,z) ) {
				lvecinos.add(new Nodo(x+1,z,nActual));
				
			}
			if ( m_Map.CanWalk(x-1,z) ) {
				lvecinos.add(new Nodo(x-1,z,nActual));
				
			}
			if ( m_Map.CanWalk(x,z+1) ) {
				lvecinos.add(new Nodo(x,z+1,nActual));
				
			}
			if ( m_Map.CanWalk(x,z-1) ) {
				lvecinos.add(new Nodo(x,z-1,nActual));
				
			}
			
			return lvecinos;
	            //consideramos que el coste es 1 siempre
				  // El métdo getCost de m_Map es también para coger el coste, porque no es lo mismo si está por agua que por tierra
				  // pero en este caso no importa, cogemos 1
			//}
			
		}
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Calculates an array of positions for patrolling. 
	 * 
	 * When this method is called, it creates an array of <tt> n</tt> random positions. For medics and fieldops, the rank of <tt> n</tt> is 
	 * [1..1]. For soldiers, the rank of <tt> n</tt> is [5..10].
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void CreateControlPoints() {

		int iMaxCP = 0;

		switch ( m_eClass ) {
		case CLASS_MEDIC:
		case CLASS_FIELDOPS:
			super.CreateControlPoints();
			break;

		case CLASS_SOLDIER:
			iMaxCP = (int) (Math.random() * 5) + 5;
			m_ControlPoints = new Vector3D [iMaxCP];
			for (int i = 0; i < iMaxCP; i++ ) {
				Vector3D ControlPoints = new Vector3D();
				while (true) {

					double x = m_Map.GetTargetX() + (25 - (Math.random() * 50));
					double z = m_Map.GetTargetZ() + (25 - (Math.random() * 50));

					if ( CheckStaticPosition(x, z) == true ) {
						ControlPoints.x = x;
						ControlPoints.z = z;
						m_ControlPoints[i] = ControlPoints;
						break;
					}
				}
			}
			break;

		case CLASS_ENGINEER:
		case CLASS_NONE:
		default:
			break;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Action to do when an agent is being shot. 
	 * 
	 * This method is called every time this agent receives a messager from agent Manager informing it is being shot.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void PerformInjuryAction() {
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Action to do when ammo or health values exceed the threshold allowed. 
	 * 
	 * This method is called when current values of ammo and health exceed the threshold allowed. These values are checked 
	 * by <tt> Launch_MedicAmmo_RequestBehaviour</tt> behaviour, every ten seconds. Perhaps it is convenient to create a 
	 * <tt> TASK_RUN_AWAY</tt> task.  
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 */
	protected void PerformThresholdAction() {
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Calculates if there is an enemy at sight. 
	 * 
	 * This method scans the list <tt> m_FOVObjects</tt> (objects in the Field Of View of the agent) looking for an enemy.
	 * If an enemy agent is found, a value of <tt> TRUE</tt> is returned and variable <tt> m_AimedAgent</tt> is updated.
	 * Note that there is no criterion (proximity, etc.) for the enemy found.
	 * Otherwise, the return value is <tt> FALSE</tt>.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 * @return <tt> TRUE</tt>: enemy found / <tt> FALSE</tt> enemy not found
	 * 
	 */
	protected boolean GetAgentToAim() {

		if ( m_FOVObjects.isEmpty() ) {
			m_AimedAgent = null;
			return false;
		}

		Iterator it = m_FOVObjects.iterator();
		while ( it.hasNext() ) {						
			CSight s = (CSight) it.next();
			if ( s.getType() >= CPack.PACK_NONE ) {
				continue;
			}

			int eTeam = s.getTeam();

			if ( m_eTeam == eTeam )
				continue;

			m_AimedAgent = s;
			return true; 
		}
		m_AimedAgent = null;
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Action to do when the agent is looking at. 
	 * 
	 * This method is called just after Look method has ended. 
	 *   
	 * <em> It's very useful to overload this method. </em>
	 * 
	 */
	protected void PerformLookAction() {
		//Busqueda de packs si los humbrales son bajos
				if ( !m_FOVObjects.isEmpty() ) {
					if((GetAmmo()<iAmmoThreshold)||(GetHealth()<iHealthThreshold)){
						Object[] list = m_FOVObjects.toArray();
						for(int i = 0;i<list.length;i++){
							CSight s = (CSight)list[i];
							if ((s.getType() == CPack.PACK_MEDICPACK)&(GetHealth()<iHealthThreshold)) {
								String sNewPosition = " ( " +s.getPosition().x + " , " + s.getPosition().y + " , " + s.getPosition().z + " ) ";
								AddTask(CTask.TASK_GOTO_POSITION , this.getAID(), sNewPosition, m_CurrentTask.getPriority() + 1);
								break;
							}
							else if ((s.getType() == CPack.PACK_AMMOPACK)&(GetAmmo()<iAmmoThreshold)){
								String sNewPosition = " ( " +s.getPosition().x + " , " + s.getPosition().y + " , " + s.getPosition().z + " ) ";
								AddTask(CTask.TASK_GOTO_POSITION, this.getAID(), sNewPosition, m_CurrentTask.getPriority() + 1);
								break;
							}
						}
					}
				}		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	// End of Methods to overload inhereted from CTroop class
	/////////////////////////////////////////////////////////////////////////////////////////////////////


	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods to overload inhereted from CSoldier class
	//

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Decides if agent accepts the CFM request 
	 * 
	 * This method is a decision function invoked when a CALL FOR MEDIC request has arrived.
	 * Parameter <tt> sContent</tt> is the content of message received in <tt> CFM</tt> responder behaviour as
	 * result of a <tt> CallForMedic</tt> request, so it must be: <tt> ( x , y , z ) ( health ) </tt>.
	 * By default, the return value is <tt> TRUE</tt>, so agents always accept all CFM requests.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 * @param _sContent
	 * @return <tt> TRUE</tt> 
	 * 
	 */
	protected boolean checkBackUpAction(String _sContent) {
		// We always go to help
		return ( true );
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////


	/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Action to do when this agent reaches the target of current task. 
	 * 
	 * This method is called when this agent goes to state <em>TARGET_REACHED</em>. If current task is <tt> TASK_GIVE_MEDICPAKS</tt>, 
	 * agent must give medic packs, but in other case, it calls to parent's method.
	 *   
	 * <em> It's very useful to overload this method. </em>
	 *   
	 * @param _CurrentTask
	 * 
	 */
	protected void PerformTargetReached(CTask _CurrentTask) {

		switch ( _CurrentTask.getType() ) {
		case CTask.TASK_NONE:
			break;

		case CTask.TASK_GIVE_MEDICPAKS:
			int iPacks = _CurrentTask.getPacksDelivered();
			super.PerformTargetReached(_CurrentTask);
			if ( iPacks != _CurrentTask.getPacksDelivered() )
				System.out.println(getLocalName()+ ": Medic has left " + (_CurrentTask.getPacksDelivered() - iPacks) + " Medic Packs");
			else
				System.out.println(getLocalName()+ ": Medic cannot leave Medic Packs");
			break;

		default:
			super.PerformTargetReached(_CurrentTask);
			break;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	// End of Methods to overload inhereted from CSoldier class
	/////////////////////////////////////////////////////////////////////////////////////////////////////


}








