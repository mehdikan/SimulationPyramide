package PLNE;

import java.io.IOException;
import java.io.PrintWriter;

import org.gnu.glpk.*;

import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
import Entite.Job;
import Entite.MachinePhysique;
import Entite.Requete;
import Entite.VM;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;

public class ModeleOrdonnancementGLPK implements GlpkCallbackListener{
	glp_prob lp;
	Cloud cloud;
	int nbJobs;
	int[] nbMapTasks;
	int[]  nbReduceTasks;
	int nbMapSlots;
	int nbReduceSlots;
	int T;
	double P[];
	int D[];
	int Tm[];
	int Tr[];
	int Am[][];
	int Ar[][];
	int O[][];

	int Fm[][];  
	int Fr[][];
	
	int indexDebY;
	int indexDebR;
	
	boolean bonneSolutionTrouve=false;
	
	Cout cout;
	
	public ModeleOrdonnancementGLPK(Cloud cloud,Cout cout,int AmIn[][],int ArIn[][]){
		////////////////////////
		this.cout=cout;
		this.cloud=cloud;
		nbJobs=cloud.getNbJobs();                
		nbMapSlots=VariablesGlobales.mapSlotsIndex;			 
		nbReduceSlots=VariablesGlobales.reduceSlotsIndex;            
		T=VariablesGlobales.T;                    
		////////////////////////
		
		nbMapTasks=new int[nbJobs];
		nbReduceTasks=new int[nbJobs];
		
		////////////////////////
		for(ClasseClients c : cloud.listeClassesClient){
		for(Requete r : c.requeteEnAttente){
			for(Job job : r.listeJobs){
				nbMapTasks[job.indexJob]=job.nombreTachesMap;
				nbReduceTasks[job.indexJob]=job.nombreTachesReduce;
			}
		}
		}
		////////////////////////
		
		P=new double[nbJobs];
		D=new int[nbJobs];
		Tm=new int[nbJobs];
		Tr=new int[nbJobs];
		Am=new int[nbJobs][];
		for(int i=0;i<nbJobs;i++)
		Am[i]=new int[nbMapTasks[i]];
		Ar=new int[nbJobs][];
		for(int i=0;i<nbJobs;i++)
		Ar[i]=new int[nbReduceTasks[i]];
		O=new int[nbJobs][nbJobs];
		
		Fm=new int[nbMapSlots][T];      
		Fr=new int[nbReduceSlots][T]; 
		
		////////////////////////
		for(int i=0;i<nbJobs;i++){	
		for(int m=0;m<nbMapTasks[i];m++){
			Am[i][m]=AmIn[i][m];
			//System.out.println("Am["+i+"]["+m+"]="+Am[i][m]);
		}
		for(int r=0;r<nbReduceTasks[i];r++){
			Ar[i][r]=ArIn[i][r];
			//System.out.println("Ar["+i+"]["+r+"]="+Ar[i][r]);
		}	
		
		for(int j=0;j<nbJobs;j++){
			O[i][j]=0;
		}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
		for(Requete rq : c.requeteEnAttente){
			for(Job job1 : rq.listeJobs){
				Tm[job1.indexJob]=job1.dureeTacheMap;
				Tr[job1.indexJob]=job1.dureeTacheReduce;
				D[job1.indexJob]=rq.dateLimite;
				if(job1==rq.jobFinal) P[job1.indexJob]=rq.poids;
				else P[job1.indexJob]=0;
			}
		}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
		for(Requete r : c.requeteEnAttente){
			for(Job job1 : r.listeJobs){
				for(Job job2 : r.listeJobs){
					O[job1.indexJob][job2.indexJob]=r.getDepandance(job1, job2);
				}
			}
		}
		}
		
		for(int t=0;t<T;t++){
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				for(int a=vm.indexDebutSlotsMap;a<vm.indexDebutSlotsMap+vm.nbMapSlots;a++){
					Fm[a][t]=vm.disponibliteTrancheTempsMap[a-vm.indexDebutSlotsMap][t];
				}
			}
		}
		
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				for(int b=vm.indexDebutSlotsReduce;b<vm.indexDebutSlotsReduce+vm.nbReduceSlots;b++){
					Fr[b][t]=vm.disponibliteTrancheTempsReduce[b-vm.indexDebutSlotsReduce][t];
				}
			}
		}
		}
		//////////////////////
	}
	
	public Gantt resoudre(){
		
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        Gantt gantt=null;

        try {
            // Create problem
            lp = GLPK.glp_create_prob();
            System.out.println("Problem créé");
            GLPK.glp_set_prob_name(lp, "Probleme ordonnancement");

            // Define columns
            
            int index=1;
            for(int i=0;i<nbJobs;i++){
            	for(int j=0;j<nbMapTasks[i];j++){
            		for(int t=0;t<T;t++){
            			 GLPK.glp_add_cols(lp, 1);
            			 GLPK.glp_set_col_name(lp, index, "X["+i+"]["+j+"]["+t+"]");
            			 GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
            			 index++;
            		}
            	}
            }
            
            indexDebY=index;
            for(int i=0;i<nbJobs;i++){
            	for(int j=0;j<nbReduceTasks[i];j++){
            		for(int t=0;t<T;t++){
            			GLPK.glp_add_cols(lp, 1);
           			 	GLPK.glp_set_col_name(lp, index, "Y["+i+"]["+j+"]["+t+"]");
           			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
           			 	index++;
            		}          	
            	}
        	}
                        
            indexDebR=index;
            for(int i=0;i<nbJobs;i++){
            	for(int t=0;t<T;t++){
            		GLPK.glp_add_cols(lp, 1);
       			 	GLPK.glp_set_col_name(lp, index, "R["+i+"]["+t+"]");
       			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
            		index++;
            	}
        	}
            
            int nbVariables=index-1;

            // Create constraints            
            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);
            int contIndex=1;
            for(int i=0;i<nbJobs;i++){
				for(int t=0;t<T;t++){				
					for(int r=0;r<nbReduceTasks[i];r++){
						GLPK.glp_add_rows(lp, 1);
						GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
						GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, 1, 1);
			            GLPK.intArray_setitem(ind, 1, getYIndex(i,r,t));
			            GLPK.intArray_setitem(ind, 2, getRIndex(i,t));
			            GLPK.doubleArray_setitem(val, 1, 1);
			            GLPK.doubleArray_setitem(val, 2, 1);
			            GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
						contIndex++;
					}
				}
			}
                
            for(int i=0;i<nbJobs;i++){
  			  for(int m=0;m<nbMapTasks[i];m++){	
  				  for(int t=0;t<T;t++){
  					  if(t<T-1){
  						  GLPK.glp_add_rows(lp, 1);
  						  GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
  						  GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, 0, 0);
  						  GLPK.intArray_setitem(ind, 1, getXIndex(i,m,t));
			              GLPK.intArray_setitem(ind, 2, getXIndex(i,m,t+1));
			              GLPK.doubleArray_setitem(val, 1, -1);
			              GLPK.doubleArray_setitem(val, 2, 1);
				          GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
				          contIndex++;
  					  }
  				  }
  			  }
  		  }
            
            for(int i=0;i<nbJobs;i++){
    			  for(int r=0;r<nbReduceTasks[i];r++){	
    				  for(int t=0;t<T;t++){
    					  if(t<T-1){
    						  GLPK.glp_add_rows(lp, 1);
    						  GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
    						  GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, 0, 0);
    						  GLPK.intArray_setitem(ind, 1, getYIndex(i,r,t));
    						  GLPK.intArray_setitem(ind, 2, getYIndex(i,r,t+1));
    						  GLPK.doubleArray_setitem(val, 1, -1);
    						  GLPK.doubleArray_setitem(val, 2, 1);
    						  GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
    						  contIndex++;
    					  }
    				  }
    			  }
            }

            for(int i=0;i<nbJobs;i++){
  			  for(int m=0;m<nbMapTasks[i];m++){
  				  for(int r=0;r<nbReduceTasks[i];r++){	
  					  for(int t=0;t<T;t++){
  						  if(t-Tm[i]>=1){
  				            ind = GLPK.new_intArray(3);
  				            val = GLPK.new_doubleArray(3);  
  							GLPK.glp_add_rows(lp, 1);
  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 0, 0);
  						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,t-Tm[i]));
  						    GLPK.intArray_setitem(ind, 2, getYIndex(i,r,t));
  						    GLPK.doubleArray_setitem(val, 1, -1);
  						    GLPK.doubleArray_setitem(val, 2, 1);
  						    GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
  						    contIndex++;
  					      }
  						  else if(t-Tm[i]<1){
  				            ind = GLPK.new_intArray(2);
  				            val = GLPK.new_doubleArray(2);
  				            GLPK.glp_add_rows(lp, 1);
  				            GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
  				            GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 0, 0);
  				            GLPK.intArray_setitem(ind, 1, getYIndex(i,r,t));
  				            GLPK.doubleArray_setitem(val, 1, 1);
  				            GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
  				            contIndex++;
  						  }
  					  }
  				  }
  			  }
  		  }
            
            
            for(int k=0;k<nbMapSlots;k++){
    			for(int t=0;t<T;t++){
    				int nbElement=0;
    				for(int i=0;i<nbJobs;i++){
    					for(int m=0;m<nbMapTasks[i];m++){
    						if(Am[i][m]==k && t-Tm[i]>=1){
    							nbElement+=2;
    						}
    						else if(Am[i][m]==k && t-Tm[i]<1){
    							nbElement+=1;
    						}
    					}
    				}
			        ind = GLPK.new_intArray(nbElement+1);
			        val = GLPK.new_doubleArray(nbElement+1);  
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, Fm[k][t], Fm[k][t]);
			        int iii=1;
    				for(int i=0;i<nbJobs;i++){
    					for(int m=0;m<nbMapTasks[i];m++){
    						if(Am[i][m]==k && t-Tm[i]>=1){
      						    GLPK.intArray_setitem(ind, iii, getXIndex(i,m,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
      						    GLPK.intArray_setitem(ind, iii, getXIndex(i,m,t-Tm[i]));
    						    GLPK.doubleArray_setitem(val, iii, -1);
    						    iii++;
    						}
    						else if(Am[i][m]==k && t-Tm[i]<1){
    							GLPK.intArray_setitem(ind, iii, getXIndex(i,m,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
    						}
    					}
    				}
    				GLPK.glp_set_mat_row(lp, contIndex, nbElement, ind, val);
			        contIndex++;
    			}
    		}
            
            for(int k=0;k<nbReduceSlots;k++){
    			for(int t=0;t<T;t++){
    				int nbElement=0;
    				for(int i=0;i<nbJobs;i++){
    					for(int r=0;r<nbReduceTasks[i];r++){
    						if(Ar[i][r]==k && t-Tr[i]>=1){
    							nbElement+=2;
    						}
    						else if(Ar[i][r]==k && t-Tr[i]<1){
    							nbElement+=1;
    						}
    					}
    				}
			        ind = GLPK.new_intArray(nbElement+1);
			        val = GLPK.new_doubleArray(nbElement+1);  
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, Fr[k][t], Fr[k][t]);
			        int iii=1;
    				for(int i=0;i<nbJobs;i++){
    					for(int r=0;r<nbReduceTasks[i];r++){
    						if(Ar[i][r]==k && t-Tr[i]>=1){
      						    GLPK.intArray_setitem(ind, iii, getYIndex(i,r,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
      						    GLPK.intArray_setitem(ind, iii, getYIndex(i,r,t-Tr[i]));
    						    GLPK.doubleArray_setitem(val, iii, -1);
    						    iii++;
    						}
    						else if(Ar[i][r]==k && t-Tr[i]<1){
    							GLPK.intArray_setitem(ind, iii, getYIndex(i,r,t));
      						    GLPK.doubleArray_setitem(val, iii, 1);
      						    iii++;
    						}
    					}
    				}
    				GLPK.glp_set_mat_row(lp, contIndex, nbElement, ind, val);
			        contIndex++;
    			}
    		}
            
            
            
            
            for(int i=0;i<nbJobs;i++){
    			for(int j=0;j<nbJobs;j++){
    			  for(int m=0;m<nbMapTasks[j];m++){
    				  for(int r=0;r<nbReduceTasks[i];r++){	
    					  for(int t=0;t<T;t++){
    						  if(i!=j && t-Tr[i]>=1){
    							    ind = GLPK.new_intArray(3);
    	  				            val = GLPK.new_doubleArray(3);  
    	  							GLPK.glp_add_rows(lp, 1);
    	  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
    	  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 1-O[i][j], 1-O[i][j]);
    	  						    GLPK.intArray_setitem(ind, 1, getXIndex(j,m,t));
    	  						    GLPK.intArray_setitem(ind, 2, getYIndex(i,r,t-Tr[i]));
    	  						    GLPK.doubleArray_setitem(val, 1, 1);
    	  						    GLPK.doubleArray_setitem(val, 2, -1);
    	  						    GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
    	  						    contIndex++;
    						  }
    						  else if(i!=j && t-Tr[i]<1){
    							  ind = GLPK.new_intArray(2);
  	  				            val = GLPK.new_doubleArray(2);  
  	  							GLPK.glp_add_rows(lp, 1);
  	  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
  	  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 1-O[i][j], 1-O[i][j]);
  	  						    GLPK.intArray_setitem(ind, 1, getXIndex(j,m,t));
  	  						    GLPK.doubleArray_setitem(val, 1, 1);
  	  						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
  	  						    contIndex++;
    						  }
    					  }
    				  }
    			  }
    			}
    		}          
            
            /*GLPK.glp_add_rows(lp, 2);
            GLPK.glp_set_row_name(lp, 1, "c1");
            GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_LO, 1, 1);

            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);

            GLPK.intArray_setitem(ind, 1, 1);
            GLPK.intArray_setitem(ind, 2, 2);
            GLPK.doubleArray_setitem(val, 1, 1);
            GLPK.doubleArray_setitem(val, 2, 1);
            GLPK.glp_set_mat_row(lp, 1, 2, ind, val);*/

            GLPK.delete_doubleArray(val);
            GLPK.delete_intArray(ind);

            // Define objective
            GLPK.glp_set_obj_name(lp, "obj");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
            for(int i=0;i<=nbVariables;i++){
            	GLPK.glp_set_obj_coef(lp, 0, 0);
            }            
            index=indexDebR;
            for(int i=0;i<nbJobs;i++){
				for(int t=0;t<T;t++){
					if(t>D[i]-Tr[i]){
						GLPK.glp_set_obj_coef(lp, index, P[i]);
					}
					index++;
				}
            }

            GlpkCallback.addListener(this);
            // Solve model
            iocp = new glp_iocp();
            GLPK.glp_init_iocp(iocp);
            iocp.setPresolve(GLPKConstants.GLP_ON);
            ret = GLPK.glp_intopt(lp, iocp);

            // Retrieve solution
            
            if (ret == 0 || bonneSolutionTrouve) {
                //write_mip_solution(lp);
            	//afficher(lp);
            	gantt=ecrireResultat(lp);
            } else {
                System.out.println("The problem could not be solved");
            };

            // free memory
            GLPK.glp_delete_prob(lp);
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
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        n = GLPK.glp_get_num_cols(lp);
        for(i=1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val  = GLPK.glp_mip_col_val(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }
    }
	
	
	
	
	/*public void afficher(glp_prob lp){	
        String name;
        double val;
		int Req[];
		Req=new int[nbJobs];
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				for(Job job1 : r.listeJobs){
					Req[job1.indexJob]=r.index;
				}
			}
		}
		
		System.out.println("Coût pénalités :"+GLPK.glp_mip_obj_val(lp));
		int index=1;
		System.out.println("X = ");
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				System.out.print("(");
				for(int t=0;t<T;t++){
					name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            //System.out.print(name);
		            //System.out.print(" = ");
		            System.out.print(val+" ");
		            index++;
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}
		
		index=indexDebY;
		System.out.println("Y = ");
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbReduceTasks[i];j++){
				//System.out.print("(");
				for(int t=0;t<T;t++){
					name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            System.out.print(name);
		            System.out.print(" = ");
		            System.out.println(val);
		            index++;
				}
				//System.out.println(")");
				System.out.println("");
				
			}
		}
		
		
		try{
		    PrintWriter writer = new PrintWriter("resultat.txt", "UTF-8");
		    int debut,fin;
		    
		    for(int i=0;i<nbJobs;i++){
				for(int j=0;j<nbMapTasks[i];j++){
					debut=0;
					fin=T-1;
					for(int t=0;t<T-1;t++){
						if(t-Tm[i]>=0){
							if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Tm[i])))< (GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp,getXIndex(i,j,t+1-Tm[i]))))
								debut=t+1;
							else if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Tm[i])))>(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1-Tm[i]))) )
								fin=t;
						}
						else{
							if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))<GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
								debut=t+1;
							else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))>GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
								fin=t;
							else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,0))==1){
								debut=1;
								fin=Tm[i];
							}
						}
					}
					writer.print("1 "+Am[i][j]+" "+Req[i]+" "+i+" "+j+" "+debut+" "+fin+" ");
				}
			}
		    
		    for(int a=0;a<nbMapSlots;a++){
		    	writer.print("1 "+a+" "+0+" "+0+" "+0+" "+0+" "+0+" ");
		    }
		    
		    for(int i=0;i<nbJobs;i++){
				for(int j=0;j<nbReduceTasks[i];j++){
					debut=0;
					fin=T-1;
					for(int t=0;t<T-1;t++){
						if(t-Tr[i]>=0){
							if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t-Tr[i])))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1-Tr[i]))))
								debut=t+1;
							else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t-Tr[i])))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1-Tr[i]))))
								fin=t;
						}
						else{
							if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t)))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))))
								debut=t+1;
							else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t)))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))))
								fin=t;
						}
					}
					writer.print("2 "+Ar[i][j]+" "+Req[i]+" "+i+" "+j+" "+debut+" "+fin+" ");
				}
			}
		    
		    
		    
		    for(int b=0;b<nbReduceSlots;b++){
		    	writer.print("2 "+b+" "+0+" "+0+" "+0+" "+0+" "+0+" ");
		    }
		    
		    writer.close();
		}
	    catch (IOException e) {
			   // do something
			}
		
		
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
	}*/
	
	
	public Gantt ecrireResultat(glp_prob lp){	
		String name;
		double val;
		int Req[];
		Req=new int[nbJobs];
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				for(Job job1 : r.listeJobs){
					Req[job1.indexJob]=r.index;
				}
			}
		}
		
		System.out.println("Coût pénalités :"+GLPK.glp_mip_obj_val(lp));
		this.cout.coutPenalite=GLPK.glp_mip_obj_val(lp);
		int index=1;
		System.out.println("X = ");
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				System.out.print("(");
				for(int t=0;t<T;t++){
					name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            //System.out.print(name);
		            //System.out.print(" = ");
		            System.out.print(val+" ");
		            index++;
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}
		
		index=indexDebY;
		System.out.println("Y = ");
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbReduceTasks[i];j++){
				System.out.print("(");
				for(int t=0;t<T;t++){
					name = GLPK.glp_get_col_name(lp, index);
		            val  = GLPK.glp_mip_col_val(lp, index);
		            //System.out.print(name);
		            //System.out.print(" = ");
		            System.out.print(val+" ");
		            index++;
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}
		
		
	    int debut,fin;
	    Gantt gantt=new Gantt();
	    
	    for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				debut=0;
				fin=T-1;
				for(int t=0;t<T-1;t++){
					if(t-Tm[i]>=0){
						if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Tm[i])))< (GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp,getXIndex(i,j,t+1-Tm[i]))))
							debut=t+1;
						else if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t-Tm[i])))>(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1-Tm[i]))) )
							fin=t;
					}
					else{
						if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))<GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
							debut=t+1;
						else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,t))>GLPK.glp_mip_col_val(lp, getXIndex(i,j,t+1)))
							fin=t;
						else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,0))==1){
							debut=1;
							fin=Tm[i];
						}
					}
				}
				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(1, Am[i][j], Req[i], i, j, debut, fin));
			}
		}
	    
	    for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbReduceTasks[i];j++){
				debut=0;
				fin=T-1;
				for(int t=0;t<T-1;t++){
					if(t-Tr[i]>=0){
						if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t-Tr[i])))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1-Tr[i]))))
							debut=t+1;
						else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t-Tr[i])))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1-Tr[i]))))
							fin=t;
					}
					else{
						if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t)))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))))
							debut=t+1;
						else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,t)))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,t+1))))
							fin=t;
					}
				}
				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(2, Ar[i][j], Req[i], i, j, debut, fin));
			}
		}
	    
	    return gantt;
	}
	
	public int getXIndex(int ii, int jj,int tt){
		int index=0;
        for(int i=0;i<nbJobs;i++){
        	if(i<ii){
        		index+=nbMapTasks[i]*T;
        	}
        	else{
        		for(int j=0;j<nbMapTasks[i];j++){
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
	
	public int getYIndex(int ii, int jj,int tt){
		int index= this.indexDebY-1;
        for(int i=0;i<nbJobs;i++){
        	if(i<ii){
        		index+=nbReduceTasks[i]*T;
        	}
        	else{
        		for(int j=0;j<nbReduceTasks[i];j++){
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

	@Override
	public void callback(glp_tree arg0) {
		// TODO Auto-generated method stub
		int reason = GLPK.glp_ios_reason(arg0);	
		
        if (reason == GLPKConstants.GLP_IBINGO || reason == GLPKConstants.GLP_IHEUR) {
            if(GLPK.glp_ios_mip_gap(arg0) <= 0.26){
            	if (reason == GLPKConstants.GLP_IBINGO) System.out.println(">>>>>>> GLP_IBINGO");
            	else if (reason == GLPKConstants.GLP_IHEUR) System.out.println(">>>>>>> GLP_IHEUR");
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
