/**
 * 
 */
package student;

/**
 * @author lauenbo
 *
 */
/**
 * @author Lau
 *
 */
/**
 * @author Lau
 *
 */
public class Nodo {
	
	private int x=0;
	private int z=0;
	private Nodo padre=null;
	private int costeG=0;
	private int costeF=0;
	
	public Nodo(int x, int z,Nodo padre)
	{
		this.x=x;
		this.z=z;
		this.padre=padre;
		
	}
	
	public void calcularCosteG ()
	{
		
		if(this.padre!=null)
		{
		this.costeG=padre.getCosteG()+1;
		}
		else this.costeG=0;
	}
	
	public int getCosteG()
	{
		return this.costeG;
	}
	
	public int getCosteF()
	{
		return this.costeF;
	}
	public int getPosX()
	{
		return this.x;
	}
	public int getPosZ()
	{
		return this.z;
	}
	
	public Nodo getPadre()
	{
		return this.padre;
	}
	
	
	public void setPadre(Nodo padre)
	{
	     this.padre =padre;
	}
	
	public boolean comparaEsNodo(Nodo nOtro)
	{
		boolean result=false;
		if (this.getPosX() ==nOtro.getPosX() )
		{
			if (this.getPosZ()== nOtro.getPosZ())
				
				result=true;
				
		}
		
		return result;
			
			
	}
	
	public int CalcularHeuristica(Nodo nFinal)
	{	
		int heuristica=Math.abs(nFinal.getPosX()-this.getPosX())+ Math.abs(nFinal.getPosZ()-this.getPosZ());
		
		return heuristica;
	}
	
	public int CalcularCosteF(Nodo nFinal)
	{
		int f,g,h;
		this.calcularCosteG();
		g=this.getCosteG();
		h=this.CalcularHeuristica(nFinal);
		f=g+h;
		this.costeF=f;
		return f;
		
		
		
	}
	
	 
	}


