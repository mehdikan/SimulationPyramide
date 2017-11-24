package PLNETEZ;

import java.io.IOException;
import java.io.PrintWriter;

import org.gnu.glpk.*;

import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
//import Entite.Job;
import Entite.StageTez;
import Entite.MachinePhysique;
//import Entite.Requete;
import Entite.RequeteTez;
import Entite.VM;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public class ModeleOrdonnancementGLPKTez implements GlpkCallbackListener{
	glp_prob lp;
	Cloud cloud;
	int nbStages;
	int[] nbTezTasks;
	int nbTezSlots;
	int T;
	double P[];
	int D[];
	int Dt[];
	int A[][];
	int F[][];
	int seq[][];
	int pip[][];
	double Ws;
	double q[];
	
	int indexDebR;
	int indexDebC;
	
	boolean bonneSolutionTrouve=false;
	
	public Cout cout;
	
	public ModeleOrdonnancementGLPKTez(Cloud cloud,Cout cout,int AIn[][]){
		////////////////////////
		this.cout=cout;
		this.cloud=cloud;
		nbStages=cloud.getNbStages();                
		nbTezSlots=cloud.tezSlotsIndex;			          
		T=VariablesGlobales.T;
		Ws=VariablesGlobales.Pstor;
		////////////////////////
		
		nbTezTasks=new int[nbStages];
		q=new double[nbStages];
		
		
		////////////////////////
		for(ClasseClients c : cloud.listeClassesClient){
		for(RequeteTez r : c.requeteTezEnAttente){
			for(StageTez stage : r.listeStages){
				nbTezTasks[stage.indexStage]=stage.nombreTachesTez;
				q[stage.indexStage]=stage.quantiteStockeApresStage;
				//System.out.println("=======================> "+Ws);
			}
		}
		}
		////////////////////////
		
		P=new double[nbStages];
		D=new int[nbStages];
		Dt=new int[nbStages];
		A=new int[nbStages][];
		for(int i=0;i<nbStages;i++)
		A[i]=new int[nbTezTasks[i]];
		
		F=new int[nbTezSlots][T];
		seq=new int[nbStages][nbStages];
		pip=new int[nbStages][nbStages];
		
		////////////////////////
		for(int i=0;i<nbStages;i++){	
			for(int m=0;m<nbTezTasks[i];m++){
				A[i][m]=AIn[i][m];
			}
		}
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez rq : c.requeteTezEnAttente){
				for(StageTez stage1 : rq.listeStages){
					Dt[stage1.indexStage]=stage1.dureeTacheTez;
					D[stage1.indexStage]=rq.dateLimite;
					if(stage1==rq.stageFinal) P[stage1.indexStage]=rq.poids;
					else P[stage1.indexStage]=0;
				}
			}
		}
		
		for(int i=0;i<nbStages;i++) {
			for(int j=0;j<nbStages;j++) {
				seq[i][j]=0;
				pip[i][j]=0;
			}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage1 : r.listeStages){
					for(StageTez stage2 : r.listeStages){
						if(r.getLien(stage1,stage2)==1)
							seq[stage1.indexStage][stage2.indexStage]=1;
						else if(r.getLien(stage1,stage2)==2)
							pip[stage1.indexStage][stage2.indexStage]=1;
					}
				}
			}
		}
		
		for(int t=0;t<T;t++){
			for(MachinePhysique mp : cloud.listeMachinesPhysique){
				for(VM vm : mp.ListeVMs){
					for(int a=vm.indexDebutSlotsTez;a<vm.indexDebutSlotsTez+vm.nbTezSlots;a++){
						F[a][t]=vm.disponibliteTrancheTempsTez[a-vm.indexDebutSlotsTez][t];
					}
				}
			}
		}
		//////////////////////
	}
	
	public Gantt resoudre(ModeleOrdonnancementGLPKTez mo){
		
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        Gantt gantt=null;

        try {
            // Create problem
            mo.lp = GLPK.glp_create_prob();
             if(!VariablesGlobales.verbose) GLPK.glp_term_out(GLPKConstants.GLP_OFF);
            if(VariablesGlobales.verbose) System.out.println("Problem créé");
            GLPK.glp_set_prob_name(mo.lp, "Probleme ordonnancement");

            // Define columns
            
            int index=1;
            for(int i=0;i<mo.nbStages;i++){
            	for(int j=0;j<mo.nbTezTasks[i];j++){
            		for(int t=0;t<mo.T;t++){
            			 GLPK.glp_add_cols(mo.lp, 1);
            			 GLPK.glp_set_col_name(mo.lp, index, "X["+i+"]["+j+"]["+t+"]");
            			 GLPK.glp_set_col_kind(mo.lp, index, GLPKConstants.GLP_BV);
            			 index++;
            		}
            	}
            }

            mo.indexDebR=index;
            for(int i=0;i<mo.nbStages;i++){
            	for(int t=0;t<mo.T;t++){
            		GLPK.glp_add_cols(mo.lp, 1);
       			 	GLPK.glp_set_col_name(mo.lp, index, "R["+i+"]["+t+"]");
       			 	GLPK.glp_set_col_kind(mo.lp, index, GLPKConstants.GLP_BV);
            		index++;
            	}
        	}
            
            mo.indexDebC=index;
            for(int i=0;i<mo.nbStages;i++){
            	for(int j=0;j<mo.nbTezTasks[i];j++){
            		for(int t=0;t<mo.T;t++){
            			 GLPK.glp_add_cols(mo.lp, 1);
            			 GLPK.glp_set_col_name(mo.lp, index, "C["+i+"]["+j+"]["+t+"]");
            			 GLPK.glp_set_col_kind(mo.lp, index, GLPKConstants.GLP_BV);
            			 index++;
            		}
            	}
            }
            
            int nbVariables=index-1;

            // Create constraints            
            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);
            int contIndex=1;
            for(int i=0;i<mo.nbStages;i++){
				for(int t=0;t<mo.T;t++){				
					for(int r=0;r<mo.nbTezTasks[i];r++){
						GLPK.glp_add_rows(mo.lp, 1);
						GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
						GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_LO, 1, 1);
			            GLPK.intArray_setitem(ind, 1, mo.getXIndex(i,r,t));
			            GLPK.intArray_setitem(ind, 2, mo.getRIndex(i,t));
			            GLPK.doubleArray_setitem(val, 1, 1);
			            GLPK.doubleArray_setitem(val, 2, 1);
			            GLPK.glp_set_mat_row(mo.lp, contIndex, 2, ind, val);
						contIndex++;
					}
				}
			}
                
            for(int i=0;i<mo.nbStages;i++){
  			  for(int m=0;m<mo.nbTezTasks[i];m++){	
  				  for(int t=0;t<mo.T;t++){
  					  if(t<mo.T-1){
  						  GLPK.glp_add_rows(mo.lp, 1);
  						  GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
  						  GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_LO, 0, 0);
  						  GLPK.intArray_setitem(ind, 1, mo.getXIndex(i,m,t));
			              GLPK.intArray_setitem(ind, 2, mo.getXIndex(i,m,t+1));
			              GLPK.doubleArray_setitem(val, 1, -1);
			              GLPK.doubleArray_setitem(val, 2, 1);
				          GLPK.glp_set_mat_row(mo.lp, contIndex, 2, ind, val);
				          contIndex++;
  					  }
  				  }
  			  }
  		  }
            
            ind = GLPK.new_intArray(4);
            val = GLPK.new_doubleArray(4);
            for(int i=0;i<mo.nbStages;i++) {
            	for(int j=0;j<mo.nbStages;j++) {
            		if(mo.seq[i][j]>0) {
	                	for(int m=0;m<mo.nbTezTasks[i];m++) {
	                		for(int r=0;r<mo.nbTezTasks[j];r++) {
	                			for(int t=0;t<mo.T;t++) {
	                				GLPK.glp_add_rows(mo.lp, 1);
	                				GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
	                				GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, 1-mo.seq[i][j], 1-mo.seq[i][j]);
	                				GLPK.intArray_setitem(ind, 1, mo.getXIndex(i,m,t));
		      			            GLPK.intArray_setitem(ind, 2, mo.getXIndex(j,r,t));
		      			            GLPK.intArray_setitem(ind, 3, mo.getCIndex(i,m,t));
		      			            GLPK.doubleArray_setitem(val, 1, mo.seq[i][j]);
		      			            GLPK.doubleArray_setitem(val, 2, -mo.seq[i][j]);
		      			            GLPK.doubleArray_setitem(val, 3, -1);
		      				        GLPK.glp_set_mat_row(mo.lp, contIndex, 3, ind, val);
		      				        contIndex++;
	                			}
                			}
                		}
                	}
                }
            }
            
            
           
            for(int i=0;i<mo.nbStages;i++) {
            	for(int j=0;j<mo.nbStages;j++) {
            		if(mo.pip[i][j]>0) {
	                	for(int m=0;m<mo.nbTezTasks[i];m++) {
	                		for(int r=0;r<mo.nbTezTasks[j];r++) {
	                			for(int t=0;t<mo.T;t++) {
	                				GLPK.glp_add_rows(mo.lp, 1);
	                				GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
	                				GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, 1-mo.pip[i][j], 1-mo.pip[i][j]);
	                				GLPK.intArray_setitem(ind, 1, mo.getXIndex(i,m,t));
		      			            GLPK.intArray_setitem(ind, 2, mo.getXIndex(j,r,t));
		      			            GLPK.intArray_setitem(ind, 3, mo.getCIndex(i,m,t));
		      			            GLPK.doubleArray_setitem(val, 1, mo.pip[i][j]);
		      			            GLPK.doubleArray_setitem(val, 2, -mo.pip[i][j]);
		      			            GLPK.doubleArray_setitem(val, 3, -1);
		      				        GLPK.glp_set_mat_row(mo.lp, contIndex, 3, ind, val);
		      				        contIndex++;
	                			}
                			}
                		}
                	}
                }
            }
            
                   
            for(int k=0;k<mo.nbTezSlots;k++){
    			for(int t=0;t<mo.T;t++){
    				int nbElement=0;
    				for(int i=0;i<mo.nbStages;i++){
    					for(int m=0;m<mo.nbTezTasks[i];m++){
    						if(mo.A[i][m]==k && t-mo.Dt[i]>=1){
    							nbElement+=2;
    						}
    						else if(mo.A[i][m]==k && t-mo.Dt[i]<1){
    							nbElement+=1;
    						}
    					}
    				}
			        ind = GLPK.new_intArray(nbElement+1);
			        val = GLPK.new_doubleArray(nbElement+1);  
			        GLPK.glp_add_rows(mo.lp, 1);
			        GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, mo.F[k][t], mo.F[k][t]);
			        int iii=1;
    				for(int i=0;i<mo.nbStages;i++){
    					for(int m=0;m<mo.nbTezTasks[i];m++){
    						if(mo.A[i][m]==k && t-mo.Dt[i]>=1){
      						    GLPK.intArray_setitem(ind, iii, mo.getXIndex(i,m,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
      						    GLPK.intArray_setitem(ind, iii, mo.getXIndex(i,m,t-mo.Dt[i]));
    						    GLPK.doubleArray_setitem(val, iii, -1);
    						    iii++;
    						}
    						else if(mo.A[i][m]==k && t-mo.Dt[i]<1){
    							GLPK.intArray_setitem(ind, iii, mo.getXIndex(i,m,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
    						}
    					}
    				}
    				GLPK.glp_set_mat_row(mo.lp, contIndex, nbElement, ind, val);
			        contIndex++;
    			}
    		}          

            for(int i=0;i<mo.nbStages;i++){
    			for(int j=0;j<mo.nbStages;j++){
    			  for(int m=0;m<mo.nbTezTasks[i];m++){
    				  for(int r=0;r<mo.nbTezTasks[j];r++){	
    					  for(int t=0;t<mo.T;t++){
    						  if(i!=j && t-mo.Dt[i]>=1){
    							    ind = GLPK.new_intArray(3);
    	  				            val = GLPK.new_doubleArray(3);  
    	  							GLPK.glp_add_rows(mo.lp, 1);
    	  							GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
    	  							GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, 1-mo.seq[i][j], 1-mo.seq[i][j]);
    	  						    GLPK.intArray_setitem(ind, 1, mo.getXIndex(j,r,t));
    	  						    GLPK.intArray_setitem(ind, 2, mo.getXIndex(i,m,t-mo.Dt[i]));
    	  						    GLPK.doubleArray_setitem(val, 1, 1);
    	  						    GLPK.doubleArray_setitem(val, 2, -1);
    	  						    GLPK.glp_set_mat_row(mo.lp, contIndex, 2, ind, val);
    	  						    contIndex++;
    						  }
    					  }
    				  }
    			   }
    			}
    		}     
            
            for(int i=0;i<mo.nbStages;i++){
    			for(int j=0;j<mo.nbStages;j++){
				  for(int r=0;r<mo.nbTezTasks[j];r++){	
					  for(int t=0;t<mo.T;t++){
						  if(i!=j && t-mo.Dt[i]<1){
							ind = GLPK.new_intArray(2);
  				            val = GLPK.new_doubleArray(2);  
  							GLPK.glp_add_rows(mo.lp, 1);
  							GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
  							GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, 1-mo.seq[i][j], 1-mo.seq[i][j]);
  						    GLPK.intArray_setitem(ind, 1, mo.getXIndex(j,r,t));
  						    GLPK.doubleArray_setitem(val, 1, 1);
  						    GLPK.glp_set_mat_row(mo.lp, contIndex, 1, ind, val);
  						    contIndex++;
						  }
					  }
				  }
    			}
    		}  

            
            for(int i=0;i<mo.nbStages;i++){
    			for(int j=0;j<mo.nbStages;j++){
    			  for(int m=0;m<mo.nbTezTasks[i];m++){
    				  for(int r=0;r<mo.nbTezTasks[j];r++){	
    					  for(int t=0;t<mo.T;t++){
    						  if(i!=j){
    							    ind = GLPK.new_intArray(3);
    	  				            val = GLPK.new_doubleArray(3);  
    	  							GLPK.glp_add_rows(mo.lp, 1);
    	  							GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
    	  							GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_UP, 1-mo.pip[i][j], 1-mo.pip[i][j]);
    	  						    GLPK.intArray_setitem(ind, 1, mo.getXIndex(j,r,t));
    	  						    GLPK.intArray_setitem(ind, 2, mo.getXIndex(i,m,t));
    	  						    GLPK.doubleArray_setitem(val, 1, 1);
    	  						    GLPK.doubleArray_setitem(val, 2, -1);
    	  						    GLPK.glp_set_mat_row(mo.lp, contIndex, 2, ind, val);
    	  						    contIndex++;
    						  }
    					  }
    				  }
    			  }
    			}
    		}
            
            for(int i=0;i<mo.nbStages;i++){
            	for(int m=0;m<mo.nbTezTasks[i];m++){
            		ind = GLPK.new_intArray(2);
			        val = GLPK.new_doubleArray(2);  
					GLPK.glp_add_rows(mo.lp, 1);
					GLPK.glp_set_row_name(mo.lp, contIndex, "c"+contIndex);
					GLPK.glp_set_row_bnds(mo.lp, contIndex, GLPKConstants.GLP_FX,1 ,1);
					GLPK.intArray_setitem(ind, 1, mo.getXIndex(i,m,mo.T-1));
					GLPK.doubleArray_setitem(val, 1, 1);
					GLPK.glp_set_mat_row(mo.lp, contIndex, 1, ind, val);
					contIndex++;
            	}
            }

            GLPK.delete_doubleArray(val);
            GLPK.delete_intArray(ind);

            // Define objective
            GLPK.glp_set_obj_name(mo.lp, "obj");
            GLPK.glp_set_obj_dir(mo.lp, GLPKConstants.GLP_MIN);
            for(int i=0;i<=nbVariables;i++){
            	GLPK.glp_set_obj_coef(mo.lp, 0, 0);
            }            
            index=mo.indexDebR;
            for(int i=0;i<mo.nbStages;i++){
				for(int t=0;t<mo.T;t++){
					if(t>mo.D[i]-mo.Dt[i]){
						GLPK.glp_set_obj_coef(mo.lp, index, mo.P[i]);
					}
					index++;
				}
            }
            index=mo.indexDebC;
            for(int i=0;i<mo.nbStages;i++) {
            	for(int m=0;m<mo.nbTezTasks[i];m++) {
            		for(int t=0;t<mo.T;t++){
            			GLPK.glp_set_obj_coef(mo.lp, index, mo.Ws*mo.q[i]);
            		}
            		index++;
            	}
            }

            GlpkCallback.addListener(mo);
            // Solve model        
            iocp = new glp_iocp();
            GLPK.glp_init_iocp(iocp);
            iocp.setPresolve(GLPKConstants.GLP_ON);
            ret = GLPK.glp_intopt(mo.lp, iocp);
   
            // Retrieve solution
            
            if (ret == 0 || mo.bonneSolutionTrouve) {
                //write_mip_solution(lp);
            	//afficher(lp);
            	gantt=mo.ecrireResultat(mo.lp);
            } else {
            	if(VariablesGlobales.verbose) System.out.println("The problem could not be solved");
            };

            // free memory
            GLPK.glp_delete_prob(mo.lp);
            //GLPK.glp_free_env();
        } catch (GlpkException ex) {
            ex.printStackTrace();
            ret = 1;
        }
        
        return gantt;
        //System.exit(ret);
	}
	
	static void write_mip_solution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lp);
        val  = GLPK.glp_mip_obj_val(lp);
        if(VariablesGlobales.verbose) System.out.print(name);
        if(VariablesGlobales.verbose) System.out.print(" = ");
        if(VariablesGlobales.verbose) System.out.println(val);
        n = GLPK.glp_get_num_cols(lp);
        for(i=1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val  = GLPK.glp_mip_col_val(lp, i);
            if(VariablesGlobales.verbose) System.out.print(name);
            if(VariablesGlobales.verbose) System.out.print(" = ");
            if(VariablesGlobales.verbose) System.out.println(val);
        }
    }
	
	public Gantt ecrireResultat(glp_prob lp){	
		String name;
		double val;
		int Req[];
		Req=new int[nbStages];
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage1 : r.listeStages){
					Req[stage1.indexStage]=r.index;
				}
			}
		}
		
		if(VariablesGlobales.verbose) System.out.println("Coût ordonnancement :"+GLPK.glp_mip_obj_val(lp));
		this.cout.coutPenaliteStockage=GLPK.glp_mip_obj_val(lp);
		int index=1;
		if(VariablesGlobales.verbose) System.out.println("X = ");
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				if(VariablesGlobales.verbose) System.out.print("(");
				for(int t=0;t<T;t++){
					name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            //System.out.print(name);
		            //System.out.print(" = ");
		            if(VariablesGlobales.verbose) System.out.print(val+" ");
		            index++;
				}
				if(VariablesGlobales.verbose) System.out.println(")");
				if(VariablesGlobales.verbose) System.out.println("");
			}
		}	
		
		index=this.indexDebC;
		if(VariablesGlobales.verbose) System.out.println("C = ");
		for(int i=0;i<nbStages;i++){
        	for(int j=0;j<nbTezTasks[i];j++){
        		if(VariablesGlobales.verbose) System.out.print("(");
        		for(int t=0;t<T;t++){
        			name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            if(VariablesGlobales.verbose) System.out.print(val+" ");
		            index++;
        		}
        		if(VariablesGlobales.verbose) System.out.println(")");
        		if(VariablesGlobales.verbose) System.out.println("");
        	}
        }
		
	    int debut,fin;
	    Gantt gantt=new Gantt();
	    
	    for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				debut=0;
				fin=T-1;
				for(int t=0;t<T-1;t++){
					if(t-Dt[i]>=0){
						if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Dt[i])))< (GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp,getXIndex(i,j,t+1-Dt[i]))))
							debut=t+1;
						else if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Dt[i])))>(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1-Dt[i]))) )
							fin=t;
					}
					else{
						if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))<GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
							debut=t+1;
						else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))>GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
							fin=t;
						else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,0))==1){
							debut=1;
							fin=Dt[i];
						}
					}
				}
				//System.out.println("--------------------------------------------# "+debut);
				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(1, A[i][j], Req[i], i, j, debut, fin));
			}
		}
	    
	    return gantt;
	}
	
	public int getXIndex(int ii, int jj,int tt){
		int index=0;
        for(int i=0;i<nbStages;i++){
        	if(i<ii){
        		index+=nbTezTasks[i]*T;
        	}
        	else{
        		for(int j=0;j<nbTezTasks[i];j++){
        			if(j<jj){
        				index+=T;
        			}
        			else{
        				index+=(tt+1);
        				break;
        			}
        		}
        		break;
        	}
        }
        return index;
	}
	
	
	public int getRIndex(int ii, int tt){
        return this.indexDebR+ii*T+tt;
	}
	
	public int getCIndex(int ii, int jj,int tt){
		int index= this.indexDebC-1;
        for(int i=0;i<nbStages;i++){
        	if(i<ii){
        		index+=nbTezTasks[i]*T;
        	}
        	else{
        		for(int j=0;j<nbTezTasks[i];j++){
        			if(j<jj){
        				index+=T;
        			}
        			else{
        				index+=(tt+1);
        				break;
        			}
        		}
        		break;
        	}
        }
        return index;
	}

	@Override
	public void callback(glp_tree arg0) {
		// TODO Auto-generated method stub
		int reason = GLPK.glp_ios_reason(arg0);	
		
        if (reason == GLPKConstants.GLP_IBINGO || reason == GLPKConstants.GLP_IHEUR) {
            if(GLPK.glp_ios_mip_gap(arg0) <= 0.26){
            	//if (reason == GLPKConstants.GLP_IBINGO) System.out.println(">>>>>>> GLP_IBINGO");
            	//else if (reason == GLPKConstants.GLP_IHEUR) System.out.println(">>>>>>> GLP_IHEUR");
            	bonneSolutionTrouve=true;
                GLPK.glp_ios_terminate(arg0);
            }
            else{
            	//afficher(lp);
            }
        	//System.out.println("GLP_IBINGO >>>>>>>>>>>>>>"+GLPK.glp_mip_obj_val(lp));
        	
            //write_mip_solution(lp);
            //GLPK.glp_ios_terminate(arg0);
        }
	}
}
