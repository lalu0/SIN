package student;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Iterator;

import es.upv.dsic.gti_ia.jgomas.CMedic;
import es.upv.dsic.gti_ia.jgomas.CPack;
import es.upv.dsic.gti_ia.jgomas.CSight;
import es.upv.dsic.gti_ia.jgomas.CTask;
import es.upv.dsic.gti_ia.jgomas.Vector3D;

/**
 * @author Lau
 *
 */
/**
 * @author Lau
 *
 */

public class MyMedic extends CMedic {
	

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private CSight aliadoAseguir=null;
	private CSight s=null;
	
	private Vector3D[] vVolverPath=new Vector3D [1000];
	private boolean bHeVuelto=false;
    
	private int iterShouldUpdateTarget=0;
	private boolean bTengoBandera=false;
	private boolean bNecesitoMedicina=false;
   
	/* (non-Javadoc)
	 * @see es.upv.dsic.gti_ia.jgomas.CMedic#setup()
	 */
	protected void setup() {

		super.setup();
		SetUpPriorities();
        
		
	//addBehaviour(new myBehaviour(this));
	//addBehaviour(new BehaviourAleatorio(this));
	//addBehaviour(new BehaviourSeguirAliado(this));
		
		
	}
	
	/**
	 * @author lauenbo
	 *
	 */
	class myBehaviour extends CyclicBehaviour {	
		/**
		 * @param a
		 */
		public myBehaviour( Agent a ) {
			super(a);
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {

			System.out.println("Position:x:"+m_Movement.getPosition().x + " y: "+m_Movement.getPosition().y+" z: "+m_Movement.getPosition().z);
			System.out.println("Distance to destination:x:"+( m_Movement.getDestination().x - m_Movement.getPosition().x )+ " y: "+ (m_Movement.getDestination().y - m_Movement.getPosition().y)+ " z: "+(m_Movement.getDestination().z - m_Movement.getPosition().z));
		}
		
	}
	

	
	/**
	 * @author lauenbo
	 * Primero creamos el comportamiento, pero el agente solo ejecutará esto hasta llegar al objetivo que le hemos marcado
	 * para que lo haga de forma cíclica tenemos hemos añadido en PerformTargetReached la tarea TASK_GOTO_POSITION
	 */
	class BehaviourAleatorio extends OneShotBehaviour {		
		public BehaviourAleatorio( Agent a ) {
			super(a);
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			
			double x=10+( Math.random()*m_Map.GetSizeX()/4);
		    double z=10+( Math.random() * m_Map.GetSizeZ()/4);
		    int y =0;
		    
		    String sNewPosition=" ( "+ (int) x+" , "+ y+" , "+ (int) z+" ) ";
		    AddTask(CTask.TASK_GOTO_POSITION , getAID(), sNewPosition,1500);	
		    System.out.println("Mi destino sigue siendo "+m_Movement.getPosition().x+" y la z: "+m_Movement.getPosition().z);
		}
	}
		
;
class BehaviourSeguirAliado extends OneShotBehaviour {		
	
	   public BehaviourSeguirAliado( Agent a ) {
			super(a);
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			
			if(aliadoAseguir == null)
			{
			aliadoAseguir=GetAgenteAliadoASeguir();
			}
			
			if(aliadoAseguir != null) 
			{
			double x=aliadoAseguir.getPosition().x;
		    double z=aliadoAseguir.getPosition().z;
		    double y =aliadoAseguir.getPosition().y;
		    
		    String sNewPosition=" ( "+ (int) x+" , "+ (int) y+" , "+ (int) z+" ) ";
		    System.out.println("Voy a seguir a "+Integer.toString(aliadoAseguir.getTeam()));
		    AddTask(CTask.TASK_GOTO_POSITION , getAID(), sNewPosition,2000);
			}
		}
	}
		
;

    protected CSight GetAgenteAliadoASeguir()
    {
    	CSight compi=null;
    	if(!m_FOVObjects.isEmpty())
    	{
    		Iterator<?> it = m_FOVObjects.iterator(); 
    		
    		while(it.hasNext() && compi==null)
    		{
    		  CSight s=(CSight) it.next();
    		  
    		  if (m_eTeam ==s.getTeam())
    		  {
    			  compi=s;
    		  }
    		}
    		
    	   	
    	}
    	return compi;
    	
    	
    }
    /*
    protected Agent getAgentForName(String name)
    {
    	ContainerController cc=new ContainerController()
    	
    }
    */
    /*
    protected ArrayList GetAgentTeam()
    {
    	
    	 * 
    	 * Realmente tiene que devolver un ArrayList
    	AMSAgentDescription amsAgentDescription = new AMSAgentDescription();
    
    	SearchConstraints searchConstraints = new SearchConstraints();
    	searchConstraints.setMaxResults(new Long("99999"));
    	               
    	try {
    	        Object agentsFound = AMSService.search(configGui.getAgent(),amsAgentDescription,  
    	searchConstraints);
    	} catch (FIPAException e) {
    	        e.printStackTrace();
    	} 
    	
    	;
    }
	*/
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods to overload inhereted from CTroop class
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
	 * Al tratarse de un médico es más eficiente que al pedir vida suelte el paquete en su misma posición él mismo
	 * 
	 */

	protected void CallForMedic() {
		String miPosicion = " ( " + m_Movement.getPosition().x + " , "+ m_Movement.getPosition().y + " , "+ m_Movement.getPosition().z + " ) ";
        this.AddTask(CTask.TASK_GIVE_MEDICPAKS, getAID(), miPosicion);//TASK_GIVE_MEDICPAKS debe de tener la mayor prioridad.
        bNecesitoMedicina=true;//para que esta tarea que es la más prioritaria se ejecute, esto hara que updateTargets devuelva true
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
	 * Si tengo la bandera o he pedido medicina o bien cada 10 iteraciones actualiza y coge la tarea más prioritaria
	 *   
	 * @return true si actualida por tarea más prioritaria o false si no. 
	 * 
	 */
	protected boolean ShouldUpdateTargets() 
	{ 
		iterShouldUpdateTarget++;
		if(iterShouldUpdateTarget % 10==0 || bTengoBandera || bNecesitoMedicina)
		{
			iterShouldUpdateTarget=0;
			if(bNecesitoMedicina) bNecesitoMedicina=false;
			return true;
			
		}
		else
		{
			return false;
		}
		}  
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
	protected void ObjectivePackTaken() {} // Should we do anything when we take the objective pack? 
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
		m_TaskPriority[CTask.TASK_GET_OBJECTIVE] = 3000;
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
	
	protected boolean GeneratePath() {

		
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
		
		GenerateEscapePosition();
		String sNewPosition = " ( " + m_Movement.getDestination().x + " , " + m_Movement.getDestination().y + " , " + m_Movement.getDestination().z + " ) "; 
		AddTask(CTask.TASK_RUN_AWAY, getAID(), sNewPosition, m_CurrentTask.getPriority() + 1);
		
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
	/* (non-Javadoc)
	 * @see es.upv.dsic.gti_ia.jgomas.CBasicTroop#GetAgentToAim()
	 */
	protected boolean GetAgentToAim() {
		return GetAgentToAim_EnemigoMasCercano();

	}
	/**
	 * Este metodo inserta en una lista los enemigos a la vista y 
	 * asigna a m_AimedAgent aquel q está más cercano,en el caso que tenga amigos a la vista, calcula la diferencia entre
	 * el ángulo del m_AimedAgent y el amigo si es <=1, no dispara
	 * En el caso que hayan enemigos a la vista devuelve true y sino false.
	 * @return
	 */
	protected boolean GetAgentToAim_EnemigoMasCercano()
	{
		m_AimedAgent = null;
		boolean bfuegoAmigo=false;
	     
		//si no tengo ningún agente en el campo de visión devuelvo false
	  	if ( m_FOVObjects.isEmpty() ) 
	  	{
			return false;
		}

		ArrayList<CSight> enemigos = new ArrayList<CSight>();
		ArrayList<CSight> amigos=new ArrayList<CSight>();
		@SuppressWarnings("unchecked")
		Iterator<?> it = m_FOVObjects.iterator();
		double distanciaMenor=Double.MAX_VALUE;
		double distancia=0f;
		CSight enemigoMascercano=null;
		while ( it.hasNext() ) 
		{						
			CSight s = (CSight) it.next();
			if ( s.getType() >= CPack.PACK_NONE ) 
			{
				continue;
			}
	
			int eTeam = s.getTeam();
			
			if ( m_eTeam != eTeam ) //si se trata de un enemigo, se añade a la lista de enemigos.
			{
				enemigos.add(s);
			}
			else // si es amigo
			{
				amigos.add(s);
			}
				
		}
		
		if(!enemigos.isEmpty())
		{			
			for(int i=0;i<=enemigos.size()-1;i++)
			{
				distancia= this.distanciaManhattan(this.m_Movement.getPosition(),enemigos.get(i));
			
			if(distancia<distanciaMenor )
			{
				enemigoMascercano=enemigos.get(i);
				distanciaMenor=distancia;
			}
			}

		
		if(!amigos.isEmpty())
		{
			for(int j=0;j<=amigos.size()-1;j++)
			{
				if(Math.abs(amigos.get(j).getAngle()-enemigoMascercano.getAngle())<=1)//si estan en el mismo ángulo de tiro o con un grado de diferencia
					                                                                  
				{
					if(this.distanciaManhattan(this.m_Movement.getPosition(),amigos.get(j))<=distanciaMenor)//y ademas está a una distancia menor que el agente enmigo
                                                                                                          //No se dispara por peligro de fuego amigo
					bfuegoAmigo=true;
					
				}
			}
			if(!bfuegoAmigo)// si no hay ningún amigo en mismo o +1 ángulo y más cerca que el enemigo se dispara
			{
				m_AimedAgent=enemigoMascercano;
			}
			
		}
		else //si no tengo amigos en el campo de visión se dispara.
		{
			m_AimedAgent=enemigoMascercano;
		}
		}
		if(m_AimedAgent != null)
		{
			return true;
		}
		else { return false;}
		
	}
	/**
	 * Este metodo inserta en una lista los enemigos a la vista y 
	 * asigna a m_AimedAgent aquel q está tiene menos vida, en el caso que tenga amigos a la vista, calcula la diferencia entre
	 * el ángulo del m_AimedAgent y el amigo si es <=1, no dispara
	 * 
	 * @return En el caso que hayan enemigos a la vista devuelve true y sino false.
	 */
	protected boolean GetAgentToAim_EnemigoMenosVida()
	{
		m_AimedAgent = null;
	     
		//si no tengo ningún agente en el campo de visión devuelvo false
	  	if ( m_FOVObjects.isEmpty() ) 
	  	{
			return false;
		}

		ArrayList<CSight> enemigos = new ArrayList<CSight>();
		ArrayList<CSight> amigos = new ArrayList<CSight>();
		boolean bfuegoAmigo=false;
		@SuppressWarnings("unchecked")
		Iterator<?> it = m_FOVObjects.iterator();
		int iMenorVida=Integer.MAX_VALUE;
		int iVida=0;
		CSight enemigoMenorVida=null;
		double distanciaAmigo=0f;
		double distanciaEnemigo=0f;
		while ( it.hasNext() ) 
		{						
			CSight s = (CSight) it.next();
			if ( s.getType() >= CPack.PACK_NONE ) 
			{
				continue;
			}
	
			int eTeam = s.getTeam();
			
			if ( m_eTeam != eTeam ) //si se trata de un enemigo, se añade a la lista de enemigos.
			{
				enemigos.add(s);
			}
			else // si es amigo
			{
				amigos.add(s);
			}
		}
		
		if(!enemigos.isEmpty())
		{			
			for(int i=0;i<=enemigos.size()-1;i++)
			{
				iVida= enemigos.get(i).getHealth();
			
			if(iVida<iMenorVida )
			{
				enemigoMenorVida=enemigos.get(i);
				iMenorVida=iVida;
			}
			}

		
		if(!amigos.isEmpty())
		{
			for(int j=0;j<=amigos.size()-1;j++)
			{
				if(Math.abs(amigos.get(j).getAngle()-enemigoMenorVida.getAngle())<=1)//si estan en el mismo ángulo de tiro o con un grado de diferencia
					                                                                  //No se dispara por peligro de fuego amigo
				{
					distanciaAmigo=this.distanciaManhattan(this.m_Movement.getPosition(),amigos.get(j));
					distanciaEnemigo=this.distanciaManhattan(this.m_Movement.getPosition(),enemigoMenorVida);
					if(distanciaAmigo<=distanciaEnemigo)
					{
					bfuegoAmigo=true;
					}
				}
			}
			if(!bfuegoAmigo)// si no hay ningún amigo en mismo o +1 ángulo se dispara
			{
				m_AimedAgent=enemigoMenorVida;
			}
			
		}
		else //si no tengo amigos en el campo de visión se dispara.
		{
			m_AimedAgent=enemigoMenorVida;
		}
		}
		if(m_AimedAgent != null)
		{
			return true;
		}
		else { return false;}
		
	}
	/**
	 * Este método calcula la distancia manhattan de la pos de este agente y de otro agente pasandolo como Csight
	 * @param position
	 * @param sight
	 * @return
	 */
	protected double distanciaManhattan(Vector3D position, CSight sight) 
	{
		// la clase CSight tiene un método getDistance(), preguntar que distancia es esa?
		double distancia=Math.abs(position.x-sight.getPosition().x)+Math.abs(position.y-sight.getPosition().y)+Math.abs(position.z-sight.getPosition().z);
		
		return distancia;
		
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
	//@SuppressWarnings("serial")
	protected void PerformLookAction() {
		
		// addBehaviour(new BehaviourSeguirAliado(this));
		
		
		
		boolean bPartnerFound = false;
		
		if ( !m_FOVObjects.isEmpty() ) {
		//@SuppressWarnings("unchecked") 
		Iterator<?> it = m_FOVObjects.iterator();
		while ( it.hasNext() && !bPartnerFound ) {
		s = (CSight) it.next();
		if ( s.getType() >= CPack.PACK_NONE ) {
		continue;
		}
		bPartnerFound = m_eTeam == s.getTeam();
		}
		if (bPartnerFound) {
		final double x = s.getPosition().x;
		final double y = s.getPosition().y;
		final double z = s.getPosition().z;
		addBehaviour( new OneShotBehaviour(this) {
		public void action () {
		String msg = " ( " + x + " , " + y + " , " + z + " ) ";
		AddTask( CTask.TASK_GOTO_POSITION, getAID(), msg, 2000);
		System.out.println("New Destination: " + msg );
		}
		});
		}
		}
		
		
		
		
			
		 
		 
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	// End of Methods to overload inhereted from CTroop class
	/////////////////////////////////////////////////////////////////////////////////////////////////////



	/////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods to overload inhereted from CMedic class
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
	protected boolean checkMedicAction(String _sContent) {
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
		case CTask.TASK_GOTO_POSITION :
			//super.PerformTargetReached(_CurrentTask);
			/*
			if(!bHeVuelto)
			{
			String posInit = " ( " + m_AStarPath[0].x + " , 0.0 , " + m_AStarPath[0].z + " ) ";
			AddTask(CTask.TASK_WALKING_PATH, getAID(), posInit, 50000);
			bHeVuelto=true;
			}
			*/
			
			double x=10+( Math.random()*m_Map.GetSizeX());
		    double z=10+( Math.random() * m_Map.GetSizeZ());
		    int y =0;
		    
		    String sNewPosition=" ( "+ (int) x+" , "+ y+" , "+ (int) z+" ) ";
		    AddTask(CTask.TASK_GOTO_POSITION , getAID(), sNewPosition,1500);	
		    
			
			
			
			
			
		    break;
		case CTask.TASK_WALKING_PATH:
			/*
			super.PerformTargetReached(_CurrentTask);
            m_iAStarPathIndex=0;
            m_AStarPath=this.getvVolverPath();
            */
            
			
		default:
			super.PerformTargetReached(_CurrentTask);
			break;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	// End of Methods to overload inhereted from CMedic class
	/////////////////////////////////////////////////////////////////////////////////////////////////////


}



