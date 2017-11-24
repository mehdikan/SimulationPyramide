package PLNETEZ;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkCallback;
import org.gnu.glpk.GlpkCallbackListener;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_tree;

import Divers.VariablesGlobales;
import Entite.ClasseClients;
import Entite.Cloud;
import Entite.Cout;
import Entite.Job;
import Entite.MachinePhysique;
import Entite.Requete;
import Entite.RequeteTez;
import Entite.StageTez;
import Entite.VM;

public class ModelePlacementGLPKTEZ  implements GlpkCallbackListener{
	Cloud cloud;
	int nbStages;
	int T;
	int[] nbTezTasks;
	int nbTezSlots;
	glp_prob lp;
	double gap=-1;
	
	double[][] DIST;
	double[][] Q;
	double[] PROCT;
	double[] MEMT;
	double[] PROCS;
	double[] MEMS;
	int Dt[];
	int F[][];  

	double Pcomm,Pproc,Pmem,PXnonPlacees,PYnonPlacees,coefRepartition;
	
	public int A[][];
	
	int indexDebZ;
	int indexDebV;
	double minV=0;
	double maxV=VariablesGlobales.T+1;
	double minZ=0;
	double maxZ=10000000;
	
	int totalNbTezTasks=0;
	
	boolean bonneSolutionTrouve=false;
	
	Cout cout;
	
	public ModelePlacementGLPKTEZ(Cloud cloud,Cout cout){
		////////////////////////
		this.cout=cout;
		this.cloud=cloud;
		nbStages=cloud.getNbStages();
		nbTezSlots=cloud.tezSlotsIndex;
		Pcomm=VariablesGlobales.Pcomm;
		Pproc=VariablesGlobales.Pproc;
		Pmem=VariablesGlobales.Pmem;
		PXnonPlacees=VariablesGlobales.PXnonPlacees;
		PYnonPlacees=VariablesGlobales.PYnonPlacees;
		coefRepartition=VariablesGlobales.coefRepartition;
		T=VariablesGlobales.T;    
		////////////////////////
		
		nbTezTasks=new int[nbStages];
		
		////////////////////////
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					nbTezTasks[stage.indexStage]=stage.nombreTachesTez;
				}
			}
		}
		////////////////////////
		
		DIST=new double[nbTezSlots][nbTezSlots];
		Q=new double[nbStages][nbStages];
		PROCT=new double[nbStages];
		MEMT=new double[nbStages];
		PROCS=new double[nbTezSlots];
		MEMS=new double[nbTezSlots];
		
		F=new int[nbTezSlots][T];      
		
		Dt=new int[nbStages];
		
		///////////////////////
		for(int a=0;a<nbTezSlots;a++){
			for(int b=0;b<nbTezSlots;b++){
				DIST[a][b]=cloud.getDistanceEntreSlots(a,b);
			}
		}
		
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbStages;j++){
				Q[i][j]=0;
			}
		}
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage1 : r.listeStages){
					for(StageTez stage2 : r.listeStages){
						Q[stage1.indexStage][stage2.indexStage]=r.getQuantiteTransfertStages(stage1, stage2);
					}
				}
			}
		}
		
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				for(int a=vm.indexDebutSlotsTez;a<vm.indexDebutSlotsTez+vm.nbTezSlots;a++){
					PROCS[a]=vm.processeurTezSlots;
					MEMS[a]=vm.memoireTezSlots;
				}
			}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez r : c.requeteTezEnAttente){
				for(StageTez stage : r.listeStages){
					PROCT[stage.indexStage]=stage.processeurTacheTez;
					MEMT[stage.indexStage]=stage.memoireTacheTez;
				}
			}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(RequeteTez rq : c.requeteTezEnAttente){
				for(StageTez stage1 : rq.listeStages){
					Dt[stage1.indexStage]=stage1.dureeTacheTez;
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
		
        ////////////////////////
		
		totalNbTezTasks=0;
		for(int i=0;i<nbStages;i++){
			totalNbTezTasks=totalNbTezTasks+nbTezTasks[i];
		}
	}
	
	public void resoudre(){
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        
		try{
            // Create problem
            lp = GLPK.glp_create_prob();
            System.out.println("Problem créé - 12/06 14:23");
            GLPK.glp_set_prob_name(lp, "Probleme placement");
            
            // Define columns
            
            int index=1;
            for(int i=0;i<nbStages;i++){
            	for(int j=0;j<nbTezTasks[i];j++){
            		for(int a=0;a<nbTezSlots;a++){
	        			 GLPK.glp_add_cols(lp, 1);
	        			 GLPK.glp_set_col_name(lp, index, "X["+i+"]["+j+"]["+a+"]");
	        			 GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
	        			 //GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, 1);
	        			 index++;
            		}
            	}
            }
                        
            indexDebZ=index;
            for(int m=0;m<nbTezSlots;m++){
            	for(int r=0;r<nbTezSlots;r++){
            		GLPK.glp_add_cols(lp, 1);
            		GLPK.glp_set_col_name(lp, index, "Z["+m+"]["+r+"]");
       			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
       			    GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minZ, this.maxZ);
       			 	index++;
            	}
            }
            
            indexDebV=index;
    		GLPK.glp_add_cols(lp, 1);
    		GLPK.glp_set_col_name(lp, index, "V");
		 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
		    GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minV, this.maxV);
		 	index++;
            
			// contraintes
            int contIndex=1;
            
			for(int i=0;i<nbStages;i++){
				for(int m=0;m<nbTezTasks[i];m++){
					for(int j=0;j<nbStages;j++){
						for(int r=0;r<nbTezTasks[j];r++){
								for(int a=0;a<nbTezSlots;a++){
									for(int b=0;b<nbTezSlots;b++){
										if(Q[i][j]>0){
											ind = GLPK.new_intArray(4);
		    	  				            val = GLPK.new_doubleArray(4);  
		    	  							GLPK.glp_add_rows(lp, 1);
		    	  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
		    	  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,Q[i][j], Q[i][j]);
		    	  						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a));
		    	  						    GLPK.intArray_setitem(ind, 2, getXIndex(j,r,b));
		    	  						    GLPK.intArray_setitem(ind, 3, getZIndex(a,b));
		    	  						    GLPK.doubleArray_setitem(val, 1, Q[i][j]);
		    	  						    GLPK.doubleArray_setitem(val, 2, Q[i][j]);
		    	  						    GLPK.doubleArray_setitem(val, 3, -1);
		    	  						    GLPK.glp_set_mat_row(lp, contIndex, 3, ind, val);
		    	  						    contIndex++;
	    	  						    }
									}
								}
							}
						}
					}
				}
			
			for(int i=0;i<nbStages;i++){
				for(int m=0;m<nbTezTasks[i];m++){
					for(int a=0;a<nbTezSlots;a++){
						ind = GLPK.new_intArray(2);
				        val = GLPK.new_doubleArray(2);
				        GLPK.glp_add_rows(lp, 1);
				        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
				        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,PROCS[a], PROCS[a]);
					    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a));
					    GLPK.doubleArray_setitem(val, 1, PROCT[i]);
					    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
					    contIndex++;
					}
				}
			}
			
			for(int i=0;i<nbStages;i++){
				for(int m=0;m<nbTezTasks[i];m++){
					for(int a=0;a<nbTezSlots;a++){
						ind = GLPK.new_intArray(2);
				        val = GLPK.new_doubleArray(2);
				        GLPK.glp_add_rows(lp, 1);
				        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
				        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,MEMS[a],MEMS[a]);
					    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a));
					    GLPK.doubleArray_setitem(val, 1, MEMT[i]);
					    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
					    contIndex++;
					}
				}
			}
			
			for(int i=0;i<nbStages;i++){
				for(int m=0;m<nbTezTasks[i];m++){
					ind = GLPK.new_intArray(nbTezSlots+1);
			        val = GLPK.new_doubleArray(nbTezSlots+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_FX,1 ,1);
			        for(int a=0;a<nbTezSlots;a++){
			        	GLPK.intArray_setitem(ind, a+1, getXIndex(i,m,a));
			        	GLPK.doubleArray_setitem(val, a+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbTezSlots, ind, val);
				    contIndex++;
				}
			}	
			
			for(int i=0;i<nbStages;i++){
				for(int a=0;a<nbTezSlots;a++){
					ind = GLPK.new_intArray(nbTezTasks[i]+1);
			        val = GLPK.new_doubleArray(nbTezTasks[i]+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,1 ,1);
			        for(int m=0;m<nbTezTasks[i];m++){
			        	GLPK.intArray_setitem(ind, m+1, getXIndex(i,m,a));
			        	GLPK.doubleArray_setitem(val, m+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbTezTasks[i], ind, val);
				    contIndex++;
				}
			}
			
			for(int a=0;a<nbTezSlots;a++){	
				double somme=0;
				for(int t=0;t<T;t++){
					somme+=(1-F[a][t]);
				}
				ind = GLPK.new_intArray(totalNbTezTasks+1+1);
		        val = GLPK.new_doubleArray(totalNbTezTasks+1+1);
		        GLPK.glp_add_rows(lp, 1);
		        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
		        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,-somme ,-somme);
		        int ii=1;
		        for(int i=0;i<nbStages;i++){
		        	for(int j=0;j<nbTezTasks[i];j++){
		        		GLPK.intArray_setitem(ind, ii, getXIndex(i,j,a));
			        	GLPK.doubleArray_setitem(val, ii, Dt[i]);
			        	ii++;
		        	}
		        }
        		GLPK.intArray_setitem(ind, ii, getVIndex());
	        	GLPK.doubleArray_setitem(val, ii, -1);
	        	GLPK.glp_set_mat_row(lp, contIndex,totalNbTezTasks+1, ind, val);
			    contIndex++;
			}
						
			// fonction objective
			GLPK.glp_set_obj_coef(lp, 0,totalNbTezTasks*PXnonPlacees);
			for(int m=0;m<nbTezSlots;m++){
				for(int r=0;r<nbTezSlots;r++){
					GLPK.glp_set_obj_coef(lp, getZIndex(m,r),Pcomm*DIST[m][r]);
				}
			}
			for(int i=0;i<nbStages;i++){
				for(int m=0;m<nbTezTasks[i];m++){
					for(int a=0;a<nbTezSlots;a++){
						GLPK.glp_set_obj_coef(lp, getXIndex(i,m,a),Dt[i]*(Pproc*PROCS[a]+Pmem*MEMS[a])-PXnonPlacees);
					}
				}
			}
			GLPK.glp_set_obj_coef(lp, getVIndex(),this.coefRepartition);
			

			GlpkCallback.addListener(this);
            // Solve model
            iocp = new glp_iocp();
            GLPK.glp_init_iocp(iocp);
            iocp.setPresolve(GLPKConstants.GLP_ON);
            ret = GLPK.glp_intopt(lp, iocp);

            // Retrieve solution
            if (ret == 0 || this.bonneSolutionTrouve) {
                //write_mip_solution(lp);
            	this.setOutput(lp);
            	afficher(lp);
            } else {
                System.out.println("The problem could not be solved");
            };
            GLPK.glp_delete_prob(lp);
		}
		catch(GlpkException ex){
            ex.printStackTrace();
            ret = 1;
		}
	}
	
	public void afficher(glp_prob lp){
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
		
		System.out.println("Objectif Placement:"+GLPK.glp_mip_obj_val(lp));
		
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				System.out.print("X["+i+"]["+j+"] = (");
				for(int a=0;a<this.nbTezSlots;a++){
					val  = GLPK.glp_mip_col_val(lp, getXIndex(i,j,a));
		            System.out.print((int)val+" ");
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}
		
		int nb=totalNbTezTasks;
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				for(int a=0;a<nbTezSlots;a++){
					nb-=GLPK.glp_mip_col_val(lp, getXIndex(i,j,a));
				}
			}
		}
		System.out.println("Nombre de taches non placees : "+nb+"/"+totalNbTezTasks);
	
		double coutTotalCommunication=0;
		
		for(int i=0;i<nbStages;i++){
			for(int m=0;m<nbTezTasks[i];m++){
				for(int j=0;j<nbStages;j++){
					for(int r=0;r<nbTezTasks[j];r++){
						for(int a=0;a<nbTezSlots;a++){
							for(int b=0;b<nbTezSlots;b++){
								if(GLPK.glp_mip_col_val(lp, getXIndex(i,m,a))==1 && GLPK.glp_mip_col_val(lp, getXIndex(j,r,b))==1){
									coutTotalCommunication+=DIST[r][m]*Q[i][j];
								}
							}
						}
					}
				}
			}
		}
		coutTotalCommunication*=VariablesGlobales.Pcomm;
		
		double coutTotalProcesseur=0;
		double coutTotalMemoire=0;
		for(int i=0;i<nbStages;i++){
			for(int m=0;m<nbTezTasks[i];m++){
				for(int a=0;a<nbTezSlots;a++){
					coutTotalProcesseur+=GLPK.glp_mip_col_val(lp, getXIndex(i,m,a))*Dt[i]*PROCS[a];
					coutTotalMemoire+=GLPK.glp_mip_col_val(lp, getXIndex(i,m,a))*Dt[i]*MEMS[a];
				}
			}
		}

		System.out.println("Cout communication : "+coutTotalCommunication);
		System.out.println("Cout processeur : "+coutTotalProcesseur);
		System.out.println("Cout mémoire : "+coutTotalMemoire);
		System.out.println("Cout Ressources "+(VariablesGlobales.Pproc*coutTotalProcesseur+VariablesGlobales.Pmem*coutTotalMemoire));
		this.cout.coutComm=coutTotalCommunication;
		this.cout.coutProcesseur=coutTotalProcesseur;
		this.cout.coutMemoire=coutTotalMemoire;
	}
	
	public void setOutput(glp_prob lp){
		A=new int[nbStages][];
		for(int i=0;i<nbStages;i++)
			A[i]=new int[nbTezTasks[i]];
		
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				for(int a=0;a<nbTezSlots;a++){
					if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,a))==1){
						A[i][j]=a;
					}
				}
			}
		}
	}
	
	public int getXIndex(int ii, int jj,int aa){
		int index=0;
        for(int i=0;i<nbStages;i++){
        	if(i<ii){
        		index+=nbTezTasks[i]*this.nbTezSlots;
        	}
        	else{
        		for(int j=0;j<nbTezTasks[i];j++){
        			if(j<jj){
        				index+=this.nbTezSlots;
        			}
        			else{
        				index+=(aa+1);
        				break;
        			}
        		}
        		break;
        	}
        }
        return index;
	}
	
	public int getZIndex(int ii, int jj){
        return this.indexDebZ+ii*nbTezSlots+jj;
	}
	
	public int getVIndex(){
        return this.indexDebV;
	}

	@Override
	public void callback(glp_tree arg0) {
		// TODO Auto-generated method stub
		int reason = GLPK.glp_ios_reason(arg0);	
		
        if (reason == GLPKConstants.GLP_IBINGO || reason==GLPKConstants.GLP_IHEUR) {
            if(GLPK.glp_ios_mip_gap(arg0) <= 0.2){
            	bonneSolutionTrouve=true;
                GLPK.glp_ios_terminate(arg0);
            	
            }
        	//System.out.println("GLP_IBINGO >>>>>>>>>>>>>>"+GLPK.glp_mip_obj_val(lp));
        	
            //write_mip_solution(lp);
            //GLPK.glp_ios_terminate(arg0);
        }
        //else if(reason==GLPKConstants.GLP_IHEUR){
        	//System.out.println("== "+gap);
        	//if(gap==-1) this.gap=GLPK.glp_ios_mip_gap(arg0);
        	//else if(GLPK.glp_ios_mip_gap(arg0)<gap){
        		//this.gap=GLPK.glp_ios_mip_gap(arg0);
        		//System.out.println("GLP_IHEUR >>>>>>>>>>>>>> "+ GLPK.glp_ios_mip_gap(arg0));
        		//afficher(this.lp);
        	//}
		//}
	}
}
