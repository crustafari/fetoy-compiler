import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;

class ASTBloquePrincipal {
  SymTable global;
  Hashtable funciones;
  ASTInstBloque main;
  ASTInstBloque inicializacionesGlobales;

  ASTBloquePrincipal(SymTable tabla,ASTInstBloque in){
    global = tabla;
    funciones = new Hashtable();
    inicializacionesGlobales = in;
    main = null;
  }

  //@ requires funciones != null;
  void add(String nombreFun,Proc e){
    funciones.put(nombreFun, new funcion(e));
  }

  void addMain(ASTInstBloque e){
    main = e;
  }
  
  public String toString(){
    String ret = global+"\n";
    ret +="Funciones\n";
    ret +=funciones.size()+"\n";
    for ( Enumeration a = funciones.keys(); a.hasMoreElements() ;){
      String w =  (String) a.nextElement();
      System.out.println(funciones.get(w));
      ret+="\n"+w+"\n"+((funcion) funciones.get(w)).p.cuerpo + "\n";
    }
    if (main != null){
      ret += "\nMAIN\n";
      main.mergeAST(inicializacionesGlobales);
      ret += main;
    }
    return ret;
  }
  
  //@ requires global != null && Global.out !=null && main != null;
  void toCode(int pr, int prf, Hashtable mensajes){
    int i = global.tamG();
    Global.out.println(".data");
    Global.out.println("str1: .asciiz \"Error en los indices de los arreglos.\"");
    Global.out.println("readBool: .asciiz \"Exception:Introduzca un booleano valido 0 o 1.\"");
    Global.out.println("str2: .asciiz\"Exception:No estas en el discriminante correcto.\"");
    Global.out.println("str3: .asciiz\"Exception:Valor invalido para el discriminante.\"");
    for ( Enumeration a = mensajes.keys(); a.hasMoreElements() ;){
      String msj = (String) a.nextElement();
      Global.out.println(msj + ": .asciiz "+mensajes.get(msj));
    }


    if (i != 0)
      Global.out.println("global: .space "+i);
    Global.out.println(".text");

    Global.out.println("main:");
    Global.out.println("la $fp, ($sp)");
    tripleta y = main.tam(new tripleta(i,0,0));
    Global.out.println("add $sp, $sp, -" + y.espacio);
    
    main.mergeAST(inicializacionesGlobales);
    main.toCode(pr, prf, "fin","fin");
    Global.out.println("fin: li $v0 10\nsyscall");
    Global.out.println("disc:");
    Global.out.println("la $a0, str2");
    Global.out.println("li $v0, 4");
    Global.out.println("syscall");
    Global.out.println("j fin");
    Global.out.println("error:");
    Global.out.println("la $a0, str1");
    Global.out.println("li $v0, 4");
    Global.out.println("syscall");
    Global.out.println("invdisc:");
    Global.out.println("la $a0, str3");
    Global.out.println("li $v0, 4");         
    Global.out.println("syscall");
    Global.out.println("j fin");
    //Agrego el codigo de las funciones
    for (Enumeration e = funciones.elements(); e.hasMoreElements();){
      Proc p = ((funcion) e.nextElement()).p;
      p.toCode(0, 0);
    }

  }
}
