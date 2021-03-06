import java.util.*;

abstract class ASTExpr {
  abstract ASTTipo getTip();
  abstract boolean toCode(int pr, int prf, String proxI);
  abstract boolean Id();
  int getValor(){ return -999999; }

  //@ invariant getTip() != null;

}

abstract class ASTExprArit extends ASTExpr {
}

class ASTExprCast extends ASTExpr {
  ASTExpr exp;
  ASTTipo tipo;

  //@ invariant tipo != null;
  //@ invariant exp != null;

  //@requires e!=null && t!=null;
  ASTExprCast(ASTExpr e, ASTTipo t){
    this.exp = e;
    this.tipo = t;
  }


  public  String toString(){
    return " ("+tipo+") "+exp+" ";
  }

  boolean Id(){
    return true; 
  }

  ASTTipo getTip(){
    return tipo; 
  }

  boolean toCode(int pr, int prf, String a1){
    String z;
    if (tipo.isEntero()){
      String actual = Registros.T[pr % Registros.maxT];
      String reg = Registros.F[prf % Registros.maxF];
      exp.toCode(pr,prf,a1);
      Global.out.println("cvt.w.s "+reg+" , "+reg);
      Global.out.println("mfc1 "+actual+" , "+reg);
    }
    else{
      String actual = Registros.F[prf % Registros.maxF];
      String reg = Registros.T[pr % Registros.maxT];
      exp.toCode(pr,prf,a1);
      Global.out.println("mtc1 "+reg+" , "+actual);
      Global.out.println("cvt.s.w "+actual+" , "+actual);
    }
    return false;
  }

}

class ASTExprAritBin extends ASTExprArit {
  String op;
  ASTExpr izq;
  ASTExpr der;
  ASTTipo tipo; 

  //@ invariant tipo != null;
  //@ invariant izq != null;
  //@ invariant der != null;
  //@ invariant op != null;

  //@requires izqa!=null && dera!=null && izqa.getTip()!=null && dera.getTip()!=null ;
  ASTExprAritBin ( String op, ASTExpr izqa, ASTExpr dera ){
    this.op = op;

    // Castea de acuerdo al tipo de las expresiones
    if (izqa.getTip().isFloat() && dera.getTip().isEntero()){
      this.izq = izqa;
      this.der = new ASTExprCast(dera, new ASTTipoFloat());
    } else if (izqa.getTip().isEntero() && dera.getTip().isFloat()){
      this.der = dera;
      this.izq = new ASTExprCast(izqa, new ASTTipoFloat());
    }
    else {
      this.izq = izqa;
      this.der = dera;
    }
    //Setea el tipo de la expresión binaria
    this.tipo = this.izq.getTip(); 
  }

  ASTTipo getTip(){ return tipo; }

  int getValor(){ 
    if (op.equals("+")) 
      return izq.getValor() + der.getValor();
    else if (op.equals("-")) 
      return izq.getValor() - der.getValor(); 
    else if (op.equals("*")) 
      return izq.getValor() * der.getValor(); 
    else if (op.equals("/")){
      //if (der.getValor() != 0)
      return izq.getValor() / der.getValor(); 
    } else 
      return -99999; 
  }

  //@ non_null
  public  String toString(){ return " "+op+"\n "+izq+" "+der; }

  public boolean toCode(int pr, int prf, String a1){ 
    if (tipo.isEntero()){
      izq.toCode(pr,prf,a1);

      String y = Registros.T[pr % Registros.maxT]; String z = Registros.T[(pr + 1) % Registros.maxT]; der.toCode(pr+1,prf,a1);

      if (op.equals("+")) 
        Global.out.println("add " + y + ", " + y +", "+ z);
      else if (op.equals("-")) 
        Global.out.println("sub " + y + ", " + y +", "+ z); 
      else if (op.equals("*")) 
        Global.out.println("mult " + y +", "+ z+"\nmflo "+y); 
      else if (op.equals("/")) 
        Global.out.println("div " + y +", "+ z+"\nmflo "+y); 
      else if (op.equals("%"))
        Global.out.println("rem " + y + ", " + y +", "+ z); 
    } else { 
      String y = Registros.F[prf % Registros.maxF]; 
      String z = Registros.F[(prf + 1) % Registros.maxF]; 
      izq.toCode(pr,prf,a1); 
      der.toCode(pr,prf+1,a1); 
      if (op.equals("+")) 
        Global.out.println("add.s " + y + ", " + y +", "+ z); 
      else if(op.equals("-"))
        Global.out.println("sub.s " + y + ", " + y +", "+ z); 
      else if (op.equals("*")) 
        Global.out.println("mul.s " + y +", "+y+", "+ z); 
      else if (op.equals("/")) 
        Global.out.println("div.s " + y +", "+y+", "+ z);
      /*else if (op.equals("%"))
        Global.out.println("rem.s " + y + ", " + y +", "+ z); Q SIGNIFICA
        MODULO ENTRE DOS FLOAT
        */
    } 
    return false; 
  }

  boolean Id(){ 
    return izq instanceof ASTExprId || der instanceof ASTExprId || izq.Id() || der.Id(); 
  }

}

class ASTExprAritUna extends ASTExprArit { String op; ASTExpr hijo; ASTTipo
  tipo;

  //@ invariant hijo != null;

  //@requires h!=null;
  ASTExprAritUna ( String t, ASTExpr h){ this.hijo = h; this.op = t; this.tipo
    = h.getTip(); }

  public /*@ non_null @*/ String toString(){ return op+" "+hijo; }

  boolean toCode(int pr, int prf, String a1){ 
    hijo.toCode(pr,prf,a1); 
    if (tipo.isEntero()) { 
      String y = Registros.T[pr % Registros.maxT];
      Global.out.println("neg "+y+ " , "+y); 
    } else { 
      String y = Registros.F[prf % Registros.maxF]; 
      Global.out.println("neg.s "+y+" , "+y); 
    } return false; 
  }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return hijo instanceof ASTExprId || hijo.Id(); }
}


class ASTExprAritCtteFloat extends ASTExprArit { float a; ASTTipo tipo;

  //@ invariant tipo != null;

  ASTExprAritCtteFloat(float a){ this.a = a; this.tipo = new ASTTipoFloat(); }

  //@ non_null
  public  String toString() { return " "+new Float(a).toString()+" "; }


  boolean isEntero(){ return true; }

  boolean Id(){ return false; }

  ASTTipo getTip(){ return tipo; }

  boolean toCode(int pr, int prf, String label){ Global.out.println("li.s "+
      Registros.F[prf % Registros.maxF] + " , "+a); return false; 
  } 
}

  class ASTExprAritCtteInt extends ASTExprArit { 
    int a; 
    ASTTipo tipo;

    //@ invariant tipo != null;

    ASTExprAritCtteInt(int a){ this.a = a; this.tipo = new ASTTipoInt(); }

    public  String toString(){ return " "+new Integer(a).toString()+" "; }

    boolean isEntero(){ return true; }

    boolean isFloat(){ return false; }

    ASTTipo getTip(){ return tipo; }

    boolean Id(){ return false; }

    int getValor(){ return a; }

    boolean toCode(int pr, int prf, String a1){ 
      Global.out.println("li " +  Registros.T[pr % Registros.maxT] + ", "+a); 
      return false; 
    }

  }


abstract class ASTExprBool extends ASTExpr { }

class ASTExprBoolBinExpr extends ASTExprBool { String op; ASTExpr izq; ASTExpr
  der; ASTTipo tipo;

  //@ invariant izq != null; @ invariant der != null;

  //@requires izqa!=null && dera!=null;
  ASTExprBoolBinExpr ( String op, ASTExpr izqa, ASTExpr dera ){ this.izq =
    izqa; this.der = dera; this.op = op; this.tipo = new ASTTipoBool(); }

  public  String toString(){ return " "+op+"\n "+izq+" "+der; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return izq instanceof ASTExprId || der instanceof ASTExprId ||
    izq.Id() || der.Id(); }

  boolean toCode(int pr, int prf, String a){ boolean us = izq.toCode(pr,prf,a);

    //Calculo su RValue if (izq instanceof ASTExprLValue)
    //((ASTExprLValue)izq).getRValue(pr, prf, izq.getTip());

    if (der.getTip().isEntero()){ 
      String y = Registros.T[ pr % Registros.maxT];
      String z = Registros.T[ (pr + 1) % Registros.maxT];
      Global.out.println(Registros.salvar(pr+1)); if (der.toCode(pr + 1,prf,a))
        us = true;

      //Calculo su RValue if (der instanceof ASTExprLValue)
      //((ASTExprLValue)der).getRValue(pr, prf, der.getTip());

      //Solo comparaciones de enteros
      if (op.equals("MENOR")){ 
        Global.out.println("slt " + y + ", " + y +", "+z); 
      } else if (op.equals("MENORIGUAL")){ 
        Global.out.println("sle " + y + ", " + y +", "+z); 
      } else if (op.equals("MAYORIGUAL")){
        Global.out.println("sge " + y + ", " + y +", "+z); 
      } else if (op.equals("MAYOR")){ 
        Global.out.println("sgt " + y + ", " + y +", "+z); 
      } else if (op.equals("IGUALIGUAL")){
        Global.out.println("seq " + y + ", " + y + ","+z); 
      }

      Global.out.println(Registros.restaurar(pr+1)); 
    } else { String y = Registros.F[ prf % Registros.maxT]; String z = Registros.F[ (prf + 1) % Registros.maxT]; String NE = Global.nuevaEtiqueta(); String NE2 = Global.nuevaEtiqueta(); Global.out.println(Registros.salvarF(prf+1));

      if (der.toCode(pr,prf + 1,a)) us = true;

      if (op.equals("MENOR")){ Global.out.println("c.lt.s "+ y +", "+z); } 
      else if (op.equals("MENORIGUAL")){ Global.out.println("c.le.s "+ y +", "+z);} 
      else if (op.equals("MAYORIGUAL")){ Global.out.println("c.le.s "+ z +", "+y); } 
      else if (op.equals("MAYOR")){ Global.out.println("c.lt.s "+ z +", "+y); } 
      else if (op.equals("IGUALIGUAL")){ Global.out.println("c.eq.s "+ y + ","+z); } 

      Global.out.println("bc1f "+NE);
      Global.out.println("li "+Registros.T[pr % Registros.maxT]+" , 1");
      Global.out.println("j "+NE2+"\n"+NE+":"); Global.out.println("li "+Registros.T[pr % Registros.maxT]+" , 0");
      Global.out.println(NE2+":");
      Global.out.println(Registros.restaurarF(prf+1)); 
    } return us; 
  } 
}

class ASTExprBoolUna extends ASTExprBool { ASTExpr hijo; ASTTipo tipo;
  //@ invariant hijo!=null;

  //@ requires h!=null;
  ASTExprBoolUna (ASTExpr h){ this.hijo = h; this.tipo = h.getTip(); }

  public /*@ non_null @*/ String toString(){ return "!"+hijo; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return hijo instanceof ASTExprId || hijo.Id(); }

  boolean toCode(int pr, int prf, String a){ 
    String y = Registros.T[pr % Registros.maxT]; 
    String NE = Global.nuevaEtiqueta(); 
    String NE2 = Global.nuevaEtiqueta(); 
    boolean us =hijo.toCode(pr,prf,a);
    Global.out.println("beqz "+y+" "+NE); 
    Global.out.println("li "+y+" 0\nj "+NE2); 
    Global.out.println(NE+":\nli "+y+" 1\n"+NE2+":"); 
    return us; 
  } 
}


class ASTExprBoolCtte extends ASTExprBool{ boolean valor; ASTTipo tipo;

  //@invariant tipo!=null;

  ASTExprBoolCtte(boolean valor){ this.valor = valor; this.tipo = new
    ASTTipoBool(); }

  public  String toString(){ return " "+new Boolean(valor).toString()+" "; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return false; }

  boolean toCode(int pr, int prf, String a){ 
    String y = Registros.T[pr % Registros.maxT]; 
    if (valor) 
      Global.out.println("li "+y+" , 1"); 
    else
      Global.out.println("li "+y+" , 0"); return false; 
  } 
}



// Chequear que ambos son operadores booleanos.
class ASTExprBoolBinBool extends ASTExprBool { 
  String op; 
  ASTExpr izq; 
  ASTExpr der; 
  ASTTipo tipo;

  //@ invariant izq!=null; @ invariant der!=null; @invariant tipo!=null;

  //@ requires izqa!=null && dera!=null; 
  ASTExprBoolBinBool ( String op, ASTExpr izqa, ASTExpr dera ){ this.izq =
    izqa; this.der = dera; this.op = op; this.tipo = new ASTTipoBool(); }

  public  String toString(){ return " "+op+"\n "+izq+" "+der; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return izq instanceof ASTExprId || der instanceof ASTExprId || izq.Id() || der.Id(); }

  boolean toCode(int pr, int prf, String a){ 
    String NE = Global.nuevaEtiqueta(); 
    String y = Registros.T[pr % Registros.maxT]; 
    if (op.equals("I")) { 
      if (izq.toCode(pr,prf,NE)){ 
        Global.out.println(NE+":");
        NE = Global.nuevaEtiqueta(); 
      } 
      String z = Registros.T[ (pr + 1) % Registros.maxT]; 
      if (der.toCode(pr + 1,prf,NE))
        Global.out.println(NE+":"); 
      Global.out.println("and " + y + ", " + y +", "+ z); 
    } else if (op.equals("O")){ 
      if (izq.toCode(pr,prf,NE)){ 
        Global.out.println(NE+":"); 
        NE = Global.nuevaEtiqueta(); 
      } String z = Registros.T[ (pr + 1) % Registros.maxT]; 
      if (der.toCode(pr+1,prf,NE))
        Global.out.println(NE+":"); 
      Global.out.println("or " + y + ", " + y +", "+ z); 
    } else if (op.equals("II")){ 
      boolean us = izq.toCode(pr, prf,NE); 
      Global.out.println("beqz "+ y +" , "+a); 
      Global.out.println(NE+":"); NE =
        Global.nuevaEtiqueta();

      if (der.toCode(pr, prf,NE)) 
        Global.out.println(NE+":"); 
      return true; 
    }
    else if (op.equals("OO")){ 
      boolean us = izq.toCode(pr, prf, NE);
      Global.out.println("beqz "+y+" , "+a); 
      if (us){
        Global.out.println(NE+":"); NE = Global.nuevaEtiqueta(); } 
      if (der.toCode(pr, prf,NE)) Global.out.println(NE+":"); return true; }
      return false; 
  } 
}

class ASTExprBoolBinString extends ASTExprBool { ASTExpr izq; ASTExpr der;
  ASTTipo tipo;

  //@ invariant izq!=null; 
  //@ invariant der!=null; 
  //@ invariant tipo!=null;

  //@requires izqa!=null && dera!=null;
  ASTExprBoolBinString ( ASTExpr izqa, ASTExpr dera ){ 
    this.izq = izqa;
    this.der = dera; 
    this.tipo = new ASTTipoBool(); 
  }

  public  String toString(){ 
    return " ==\n "+izq+" "+der; 
  }

  ASTTipo getTip(){ 
    return tipo; 
  }

  boolean Id(){ 
    return izq instanceof ASTExprId || der instanceof ASTExprId || izq.Id() || der.Id(); 
  }

  boolean toCode(int pr, int prf, String a){ return false; 
  } 
}

class ASTExprBoolBinChar extends ASTExprBool { ASTExpr izq; ASTExpr der;
  ASTTipo tipo;
  String op;

  //@ invariant izq!=null; 
  //@ invariant der!=null; 
  //@ invariant tipo!=null;

  //@requires izqa!=null && dera!=null;
  ASTExprBoolBinChar ( String op, ASTExpr izqa, ASTExpr dera){ 
    this.op = op;
    this.izq = izqa; 
    this.der = dera; 
    this.tipo = new ASTTipoBool(); 
  }

  public  String toString(){ return op + " ==\n "+izq+" "+der; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ 
    return izq instanceof ASTExprId || der instanceof ASTExprId || izq.Id() || der.Id(); 
  }

  boolean toCode(int pr, int prf, String a){ 
    String actual= Registros.T[(pr) % Registros.maxT ]; 
    Global.out.println(Registros.salvar(pr+1));
    izq.toCode(pr,prf,a); der.toCode(pr+1,prf,a); 
    Global.out.println("seq "+actual+" , "+actual+" , "+Registros.T[(pr+1)%Registros.maxT]);
    Global.out.println(Registros.restaurar(pr+1)); 
    return false; 
  } 
}


abstract class ASTExprString extends ASTExpr {
  String etiqueta;
}

// Chequear que ambos sean string
class ASTExprStringBin extends ASTExprString{
  ASTExpr izq; 
  ASTExpr der; 
  ASTTipo tipo;
  //@ invariant izq!=null; @ invariant der!=null; @ invariant tipo!=null;

  //@requires izqa!=null && dera!=null;
  ASTExprStringBin(String e, ASTExpr izqa, ASTExpr dera){ 
    this.etiqueta = e;
    this.izq = izqa; 
    this.der = dera;
     
    this.tipo = new ASTTipoString(); 
  }

  //Sin etiqueta
  ASTExprStringBin(ASTExpr izqa, ASTExpr dera){ 
    this.izq = izqa; 
    this.der = dera;
     
    this.tipo = new ASTTipoString(); 
  }


  public /*@ non_null @*/ String toString(){ return "+\n "+izq+" "+der; }

  ASTTipo getTip(){ return tipo; }

  boolean Id(){ return izq instanceof ASTExprId || der instanceof ASTExprId ||
    izq.Id() || der.Id(); }

  String getString(){
    return "";
  }
  /*Supongo que en pr viene la direccion del primer string y en pr+1 la dir del 
   * segundo string 
   */
  boolean toCode(int pr, int prf, String a){
    izq.toCode(pr,prf,a);
    der.toCode(pr+1,prf,a);
    String NE = Global.nuevaEtiqueta();
    String NE2 = Global.nuevaEtiqueta();
    String NE3 = Global.nuevaEtiqueta();
    String NE4 = Global.nuevaEtiqueta();
    String NE5 = Global.nuevaEtiqueta();
    String NE6 = Global.nuevaEtiqueta();
    String NE7 = Global.nuevaEtiqueta();
    String NE8 = Global.nuevaEtiqueta();
    //Registros donde esta el apuntador al primer string
    String reg = Registros.T[pr % Registros.maxT];
    //Registros donde esta el apuntador al segundo string
    String preg = Registros.T[(pr +1) % Registros.maxT];
    //Registro donde se verificara si se acabo el string.
    String sreg = Registros.T[(pr+2) % Registros.maxT];
    //Registro donde se iterara el String.
    String dreg = Registros.T[(pr+3) % Registros.maxT];
    //Registros donde se llevara el tamano de lo dos string.
    String creg = Registros.T[(pr+4) % Registros.maxT];

    Global.out.println("li "+creg+", -1");
    Global.out.println("move "+dreg+" , "+reg);
    Global.out.println("add "+dreg+" , "+dreg+" , -1");
    Global.out.println(NE+":");
    Global.out.println("add "+dreg+" , "+dreg+" , 1");
    Global.out.println("add "+creg+" , "+creg+" , 1");
    Global.out.println("lb "+sreg+" , 0("+dreg+")");
    Global.out.println("beqz "+sreg+" "+NE2);
    Global.out.println("j "+NE);
    Global.out.println(NE2+":");
    Global.out.println("move "+dreg+" ,"+preg);
    Global.out.println("add "+dreg+" , "+dreg+" , -1");
    Global.out.println("add "+creg+" , "+creg+" , -1");
    Global.out.println(NE3+":");
    Global.out.println("add "+dreg+" , "+dreg+" , 1");
    Global.out.println("add "+creg+" , "+creg+" , 1");
    Global.out.println("lb "+sreg+" , 0("+dreg+")");
    Global.out.println("beqz "+sreg+" "+NE4);
    Global.out.println("j "+NE3);
    Global.out.println(NE4+":");
//suma uno al creg (que es el contador del tamano de los arreglos para 
//el caracter nulo al final del string.
    Global.out.println("add "+creg+" , "+creg+" , 1");
    Global.out.println("move $a0 "+creg);
    Global.out.println("li $v0 9\nsyscall");
    //aqui ne creg estoy guardando el nuevo espacio de memoria.
    Global.out.println("move "+creg+" $v0");
    Global.out.println("move "+dreg+" $v0");
    //aqui empiezo a usar a reg como el dommy para recorrer los string.
    Global.out.println("add "+reg+" , "+reg+" , -1");
    Global.out.println("add "+creg+" , "+creg+" , -1");
    Global.out.println(NE5+":");
    Global.out.println("add "+reg+" , "+reg+" , 1");
    Global.out.println("add "+creg+" , "+creg+" , 1");
    Global.out.println("lb "+sreg+" , 0("+reg+")");
    Global.out.println("beqz "+sreg+" "+NE6);
    Global.out.println("sb "+sreg+" 0("+creg+")");
    Global.out.println("j "+NE5);
    Global.out.println(NE6+":");
    Global.out.println("add "+preg+" , "+preg+" , -1");
    Global.out.println("add "+creg+" , "+creg+" , -1");
    Global.out.println(NE7+":");
    Global.out.println("add "+creg+" , "+creg+" , 1");
    Global.out.println("add "+preg+" , "+preg+" , 1");
    Global.out.println("lb "+sreg+" , 0("+preg+")");
    Global.out.println("beqz "+sreg+" "+NE8);
    Global.out.println("sb "+sreg+" 0("+creg+")");
    Global.out.println("j "+NE7);
    Global.out.println(NE8+":");
    Global.out.println("move "+reg+" "+dreg);
    return false;
  }
}

class ASTExprStringCtte extends ASTExprString {
  String ctte; 
  ASTTipo tipo;
  //@ invariant tipo!=null;

  ASTExprStringCtte(String label, String a){ 
    ctte = a;
    etiqueta = label; 
    this.tipo = new ASTTipoString(); 
  }

  public /*@ non_null @*/ String toString(){ 
    return ctte; 
  }

  boolean Id(){ 
    return false; 
  }

    String getString(){
      return ctte;
    }

    boolean toCode(int pr, int prf, String a){
      //Tamano a reservar en memoria para guardar el string para imprimirlo.
      //el +1 es para que sepa el final de string.
      int tam = ctte.length() -1;
      String reg = Registros.T[pr % Registros.maxT];
      String reg1 = Registros.T[(pr+1) % Registros.maxT];
      //Reservo espacio para el String.
      Global.out.println("li $a0 , "+tam);
      Global.out.println("li $v0, 9\nsyscall");
      //Copiamos en pr la direccion del espacio que me asignaron para ir guardando
      //letra por letra en esa direccion.
      Global.out.println("move "+reg+" $v0");
      for (int i =1;i < ctte.toCharArray().length - 1;i++){
        Global.out.println("li "+reg1+" , "+((int) ctte.toCharArray()[i]));
        Global.out.println("sb "+reg1+" "+(i-1)+"("+reg+")");
      }
      return false;
    }
  ASTTipo getTip(){
    return tipo;
  }
}

abstract class ASTExprChar extends ASTExpr{
}

class ASTExprCharCtte extends ASTExprChar{
  char i;
  ASTTipo tipo;

  //@ invariant tipo!=null;
  ASTExprCharCtte(char h){
    this.i = h;
    this.tipo = new ASTTipoChar();
  }

  public /*@ non_null @*/ String toString(){
    return "'"+new Character(i).toString()+"'";
  }

  ASTTipo getTip(){
    return tipo;
  }

  boolean Id(){
    return false;
  }

  boolean toCode(int pr, int prf, String a){
    String y = Registros.T[pr % Registros.maxT];
    Global.out.println("li "+y+", "+(int) i);
    return false;
  }
}


abstract class ASTExprArray extends ASTExpr{
  LinkedList values;
  ASTTipo tipo;
  abstract ASTTipo getTip();
}

class ASTExprArrayCtte extends ASTExprArray {
  LinkedList values;
  ASTTipo tipo;

  //@ invariant values!=null;
  //@ invariant tipo!=null;

  //@ requires a!=null;
  ASTExprArrayCtte(LinkedList a){
    values  = a;

    if (a.getFirst() instanceof ASTExpr)
      tipo = new ASTTipoArray(((ASTExpr) a.getFirst()).getTip(), new ASTExprAritCtteInt(a.size()));
    else
      tipo = new ASTTipoArray(((ASTExpr) a.getFirst()).getTip(), new ASTExprAritCtteInt(a.size()));
  }

  public /*@ non_null @*/ String toString(){
    String ret = "[";
    LinkedList l = values;
    if (l != null){
      for (int i = 0; i < l.size(); i++){
        if (l.get(i) instanceof ASTExpr){
          ASTExpr e = (ASTExpr) l.get(i);
          if (e!=null)
            ret += e+",";
        }
      }
      ret += "]";
    }
    return ret;
  }

  ASTTipo getTip(){
    return tipo;
  }

  boolean Id(){
    return false;
  }

  boolean toCode(int pr, int prf, String a){
    return false;
  }

  void toCodeI(int pr, int prf, String a, int i){
    String reg = Registros.T[pr % Registros.maxT];
    if (values.get(i) instanceof ASTExpr) {
      ((ASTExpr) values.get(i)).toCode(pr, prf, a);
    } else
      Global.out.println("li " + reg + "," + values.get(i));
  }
}

class ASTExprArrayId extends ASTExprArray{
  ASTExprLValue id;
  LinkedList values;
  ASTTipo tipo;

  //@ invariant id!=null;
  //@ invariant values!=null;
  //@ invariant tipo!=null;

  //@ requires h!=null && t!=null; 
  ASTExprArrayId(ASTExprLValue h, ASTTipo t){
    this.id = h;
    values  = new LinkedList();
    tipo = t;
  }

  public /*@ non_null @*/ String toString(){
    return id.toString();
  }

  boolean Id(){
    return true;
  }

  ASTTipo getTip(){
    return tipo;
  }

  boolean toCode(int pr, int prf, String a){
    return false;
  }
}

class ASTExprArrayElem extends ASTExprLValue {
  ASTExprLValue lvalue;
  ASTExpr index;
  ASTTipo tipo;

  //@ invariant index!=null;
  //@ invariant tipo!=null;

  //@ requires t!=null && i!=null;
  ASTExprArrayElem(ASTExprLValue l, ASTTipo t, ASTExpr i){
    this.lvalue = l;
    this.tipo = t;
    this.index = i;
  }

  public /*@ non_null @*/ String toString(){
    return lvalue + "[" + index + "]";
  }

  boolean Id(){
    return true;
  }

  String getId(){
    return id;
  }

  ASTTipo getTip(){
    return tipo;
  }

  ASTTipo getTipI(){
    return lvalue.getTip();
  }

  info getInfo(){
    return inf;
  }

  //Modifica el valor en la referencia
  void modifica(int pr, int prf){
    String actual = Registros.T[pr % Registros.maxT], siguiente="", move = "", store ="";
    if (tipo.isEntero()){
      siguiente = Registros.T[(pr + 1) % Registros.maxT];
      move = "move ";
      store = "sw ";
    } else if (tipo.isFloat()){
      siguiente = Registros.F[(prf + 1) % Registros.maxF];
      move = "mov.s ";
      store = "s.s ";
    }

    //Toma en cuenta Si la variable esta en el main o si esta en la pila
    Global.out.println(store + siguiente + ", ("+ actual + ")" );
  }

  void cargaDireccion(int pr, int prf, String a){
    String reg = Registros.T[pr % Registros.maxT];    
    String reg2 = Registros.T[(pr + 1) % Registros.maxT];    
    String reg3 = Registros.T[(pr + 2)% Registros.maxT];    

    lvalue.cargaDireccion(pr,prf, a);
    Global.out.println(Registros.salvar(pr+1));
    index.toCode(pr + 1,prf,a);

    //Chequeo de Indice negativo
    Global.out.println("bltz " + reg2 + ", " + Global.error);
    Global.out.println(Registros.salvar(pr+2));

    //Chequeo de si se pasó del tamaño del arreglo.
    Global.out.println("li " + reg3 +", "+ ((ASTTipoArray)lvalue.getTip()).getTam());
    Global.out.println("sub " + reg3 + ", " + reg3 + ", " + reg2); 
    Global.out.println("blez " + reg3 + ", " + Global.error);

    //Operacion de Indice 
    Global.out.println("li " + reg3 +", -"+ ((ASTTipoArray)lvalue.getTip()).subclass.tam);
    Global.out.println("mul " + reg2 +", "+reg2+", " + reg3);

    Global.out.println(Registros.restaurar(pr+2));
    Global.out.println("add " + reg +", " + reg +", " + reg2);
    Global.out.println(Registros.restaurar(pr+1));
  }

  boolean toCode(int pr, int prf, String a){
    cargaDireccion(pr, prf, a);
    getRValue(pr, prf, tipo);

    return false;
  }
}

abstract class ASTExprLValue extends ASTExpr{
  String id;
  ASTTipo tipo;
  public info inf;
  void modifica(int pr, int prf){ }
  void cargaDireccion(int pr, int prf, String a){ }
  abstract String getId();
  abstract ASTTipo getTip();
  abstract info getInfo();
  abstract ASTTipo getTipI();

  //alculas el rvalue de los lvalue
  void getRValue(int pr, int prf, ASTTipo t){
    String reg = Registros.T[pr % Registros.maxT];
    String regF = Registros.F[prf % Registros.maxF];

    if (this instanceof ASTExprLValue){ 
      if (this instanceof ASTExprId){ 
        if (!inf.onreg)
          if  (inf.obj.isFloat()){
            Global.out.println("l.s " + regF + ",(" + reg + ")" );
          } else
            Global.out.println("lw " + reg + ",(" + reg + ")" );
      } else if (t.isFloat()){
        Global.out.println("l.s " + regF + ",(" + reg + ")" );
      } else {
        Global.out.println("lw " + reg + ",(" + reg + ")" );
      }
    }
  }
}

class ASTExprId extends ASTExprLValue {
  //@ invariant tipo!=null;

  //@ requires t!=null;
  ASTExprId(String id, info t){
    this.id = id;
    if (t !=null)
      this.tipo = t.obj;
    this.inf = t;
  }

  public /*@ non_null @*/ String toString(){
    return id;
  }

  boolean Id(){
    return false;
  }

  String getId(){
    return id;
  }

  ASTTipo getTip(){
    return tipo;
  }

  ASTTipo getTipI(){
    return tipo;
  }

  info getInfo(){
    return inf;
  }

  void setInfo(info f){
    this.inf = f;
    this.tipo = f.obj;
  }

  void modifica(int pr, int prf){
    inf.modificaDireccion(pr,prf);
  }

  void cargaDireccion(int pr, int prf, String a){
    inf.cargaDireccion(pr,prf);
  }

  boolean toCode(int pr, int prf, String a){
    cargaDireccion(pr,prf, a);
    getRValue(pr, prf, inf.obj);

    return false;
  }

  int getTam(){
    return ((ASTTipoArray) tipo).getTam();
  }
}

abstract class ASTExprStruct extends ASTExpr{
}

class ASTExprStructId extends ASTExprStruct{
  ASTExprLValue id;
  info tipo;

  //@ invariant id!=null;
  //@ invariant tipo!=null;

  //@ requires h!=null && t!=null; 
  ASTExprStructId(ASTExprLValue h, info t){
    this.id = h;
    tipo = t;
  }

  public /*@ non_null @*/ String toString(){
    return id.toString();
  }

  boolean Id(){
    return true;
  }

  ASTTipo getTip(){
    return tipo.obj;
  }

  boolean toCode(int pr, int prf, String a){
    return false;
  }
}

// Esta clase representa un elemento de una estructura, usada como rvalue. Ej: Carro.caucho.color
class ASTExprStructElem extends ASTExprLValue {
  info inf;
  String id;
  ASTTipo tipo;
  ASTExprLValue lvalue;
  //@ invariant tipo!=null;
  //@ invariant hijos!=null;

  ASTExprStructElem(ASTExprLValue l, String i, ASTTipo t, info info1){
    this.id = i;
    this.lvalue = l;
    this.tipo = t;
    this.inf = info1;
  }


  public /*@ non_null @*/ String toString(){
    return  lvalue + "." + id;
  }

  boolean Id(){
    return true;
  }

  ASTTipo getTip(){
    return tipo;
  }

  ASTTipo getTipI(){
    return lvalue.getTip();
  }

  String getId(){
    return id;
  }

  info getInfo(){
    return inf;
  }

  void cargaDireccion(int pr, int prf, String a) {
    //Cargo la direccion del lvalue
    lvalue.cargaDireccion(pr,prf, a);
    String reg = Registros.T[pr % Registros.maxT];    
    String reg2 = Registros.T[(pr + 1) % Registros.maxT];    
    String reg3 = Registros.T[(pr + 2) % Registros.maxT];    
    Global.out.println(Registros.salvar(pr+1));
    Global.out.println(Registros.salvar(pr+2));

    //Chequeo dinamico del union
    if (inf.havedis){
      int desp = ((ASTTipoStruct) lvalue.getTip()).getDis();
      if(inf.disValido !=null)
        inf.disValido.toCode(pr+1,pr,a);
      else
System.out.println("Error el discriminante del info es null");
      
      Global.out.println("add "+reg3+" , "+reg+" , -"+desp);
      Global.out.println("lw "+reg3+" 0("+reg3+")");
      Global.out.println("bne "+reg3+" "+reg2+" disc");
    }
    //Cargo el desplazamiento del atributo
    Global.out.println("li "+ reg2 + ",-" + inf.desp);

    //Carga la direccion del lvalue
    Global.out.println("add "+ reg + "," + reg + "," +reg2);
    Global.out.println(Registros.restaurar(pr+1));
  }

  //Modifica el valor en la referencia
  void modifica(int pr, int prf){
    String actual = Registros.T[pr % Registros.maxT], siguiente="", move = "", store ="";
       if (tipo.isFloat()){
      siguiente = Registros.F[(prf + 1) % Registros.maxF];
      move = "mov.s ";
      store = "s.s ";
    } else {
      siguiente = Registros.T[(pr + 1) % Registros.maxT];
      move = "move ";
      store = "sw ";
    } 


    //Toma en cuenta Si la variable esta en el main o si esta en la pila
    Global.out.println(store + siguiente + ", ("+ actual + ")" );
  }

  boolean toCode(int pr, int prf, String a){
    cargaDireccion(pr,prf, a);
    getRValue(pr, prf, tipo);
    return false;
  }
}

class ASTExprFun extends ASTExprLValue {
  ASTInst ai;
  ASTTipo tipo;

  ASTExprFun(ASTTipo t, ASTInst i){
    tipo = t;
    this.ai = i;
  }

  ASTTipo getTip(){
    return tipo;
  }

  ASTTipo getTipI(){
    return tipo;
  }

  boolean toCode(int pr, int prf, String proxI){
    String reg = Registros.T[pr % Registros.maxT];
    ai.toCode(pr, prf, proxI,"fin");
    Global.out.println("add $sp, $sp, " + tipo.tam);
    Global.out.println("lw " +reg +", ($sp)");
    
    return false;
  }

  boolean Id(){
    return false;
  }

  void cargaDireccion(int pr, int prf, String a) {
    String reg = Registros.T[pr % Registros.maxT];
    ai.toCode(pr, prf, a, "fin");
    Global.out.println("add $sp, $sp, " + tipo.tam);
    Global.out.println("la " +reg +", ($sp)");
  }

  info getInfo(){
    return null;
  }

  String getId(){
    return null;
  }


}

class ASTExprReadInt extends ASTExpr {
  ASTExprReadInt(){

  }

  ASTTipo getTip(){
    return new ASTTipoInt();
  }

  boolean toCode(int pr, int prf,String proxI){
    String reg = Registros.T[pr % Registros.maxT];
    Global.out.println("li $v0 5");
    Global.out.println("syscall");
    Global.out.println("move "+reg+" $v0");
    return false;
  }

  boolean Id(){
    return false;
  }

  int getValor(){
    return 4;
  }

}

class ASTExprReadBool extends ASTExpr {
  ASTExprReadBool(){

  }
    
  boolean toCode(int pr, int prf,String proxI){
    String reg = Registros.T[pr % Registros.maxT];
    String b = Global.nuevaEtiqueta();
    Global.out.println("li $v0 , 5\nsyscall");
    Global.out.println("move "+reg+" $v0");
    Global.out.println("beqz $v0 "+b);
    Global.out.println("li $v0 , 1");
    Global.out.println("beq $v0 "+reg+" "+b);
    Global.out.println("li $v0, 4\nla $a0 ,readBool\nsyscall");
    Global.out.println("j fin");
    Global.out.println(b+":");
    return true;
  }

  boolean Id(){
    return false;
  }

  int getValor(){
    return 4;
  }

  ASTTipo getTip(){
    return new ASTTipoBool();
  }
}

class ASTExprReadChar extends ASTExpr {
  ASTExprReadChar(){

  }
    
  boolean toCode(int pr, int prf,String proxI){
    //Devuelvo en pr la direccion donde esta guardado el char.
    String reg = Registros.T[pr % Registros.maxT];
    Global.out.println("li $a0 2");
    Global.out.println("li $v0 9\nsyscall");
    Global.out.println("move "+reg+" $v0");
    Global.out.println("move $a0 , $v0");
    Global.out.println("li $a1 2");
    Global.out.println("li $v0 8\nsyscall");
    Global.out.println("lb "+reg+" 0("+reg+")");
    return false;
  }

  boolean Id(){
    return false;
  }

  int getValor(){
    return 4;
  }


  ASTTipo getTip(){
    return new ASTTipoChar();
  }
}

class ASTExprReadFloat extends ASTExpr {
  ASTExprReadFloat(){

  }
    
  boolean toCode(int pr, int prf,String proxI){
    String reg = Registros.F[prf % Registros.maxF];
    Global.out.println("li $v0 6\nsyscall");
    Global.out.println("mov.s "+reg+" $f0" );
    return false;
  }

  boolean Id(){
    return false;
  }

  int getValor(){
    return 4;
  }

  ASTTipo getTip(){
    return new ASTTipoFloat();
  }

}

class ASTExprReadString extends ASTExprString {
  ASTExpr tam;
  ASTExprReadString(ASTExpr t){
    tam = t;
  }
    
  boolean toCode(int pr, int prf,String proxI){
    String reg = Registros.T[pr % Registros.maxT];
    tam.toCode(pr,prf,proxI);
    //Movemos el tamano del string a $a0.
    Global.out.println("move $a0 , "+reg);
    //Cargamos en $v0 9 para pedir espacio de tamano $a0.
    Global.out.println("li $v0 9\nsyscall");
    //El apuntador nos los da el $v0, y lo movemos a $a0.
    Global.out.println("move $a0 , $v0");
    //Movemos a $a1 el tamano del string que queremos leer por consola.
    Global.out.println("move $a1 , "+reg);
    //Cargamos en $v0 8 para leer un string por consola
    Global.out.println("li $v0 8\nsyscall");
    //Cargamos en pr el aputandor al string leido.
    Global.out.println("move "+reg+" , $a0"); 
    return false;
  }

  boolean Id(){
    return false;
  }

  int getValor(){
    return 4;
  }

  ASTTipo getTip(){
    return new ASTTipoString();
  }

}
