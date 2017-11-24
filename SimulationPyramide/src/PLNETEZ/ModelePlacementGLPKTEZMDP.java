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

public class ModelePlacementGLPKTEZMDP implements GlpkCallbackListener{
	Cloud cloud;
	int nbStages;
	int T;
	int[] nbTezTasks;
	int nbTezSlots;
	glp_prob lp;
	double gap=-1;
	
	public boolean resolu=true;
	
	double[][] DIST;
	double[][] Q;
	double[] PROCT;
	double[] MEMT;
	double[] PROCS;
	double[] MEMS;
	int[] ACTS;
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
	
	public ModelePlacementGLPKTEZMDP(Cloud cloud,Cout cout){
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
		ACTS=new int[nbTezSlots];
		
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
					ACTS[a]=vm.ressourceActive(a-vm.indexDebutSlotsTez);
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
	
	public void resoudre(ModelePlacementGLPKTEZMDP mp){
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        
		try{
            // Create problem
			//System.out.println("1--");
			mp.resolu=true;
			mp.lp = GLPK.glp_create_prob();
            if(!VariablesGlobales.verbose) GLPK.glp_term_out(GLPKConstants.GLP_OFF);
            if(VariablesGlobales.verbose) System.out.println("Problem créé - 12/06 14:23");
            GLPK.glp_set_prob_name(mp.lp, "Probleme placement");
            
            // Define columns
            //System.out.println("2--");
            int index=1;
            for(int i=0;i<mp.nbStages;i++){
            	for(int j=0;j<mp.nbTezTasks[i];j++){
            		for(int a=0;a<mp.nbTezSlots;a++){
	        			 GLPK.glp_add_cols(mp.lp, 1);
	        			 GLPK.glp_set_col_name(mp.lp, index, "X["+i+"]["+j+"]["+a+"]");
	        			 GLPK.glp_set_col_kind(mp.lp, index, GLPKConstants.GLP_BV);
	        			 //GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, 1);
	        			 index++;
            		}
            	}
            }
                        
            mp.indexDebZ=index;
            for(int m=0;m<mp.nbTezSlots;m++){
            	for(int r=0;r<mp.nbTezSlots;r++){
            		GLPK.glp_add_cols(mp.lp, 1);
            		GLPK.glp_set_col_name(mp.lp, index, "Z["+m+"]["+r+"]");
       			 	GLPK.glp_set_col_kind(mp.lp, index, GLPKConstants.GLP_IV);
       			    GLPK.glp_set_col_bnds(mp.lp, index, GLPKConstants.GLP_DB, mp.minZ, mp.maxZ);
       			 	index++;
            	}
            }
            
            mp.indexDebV=index;
    		GLPK.glp_add_cols(mp.lp, 1);
    		GLPK.glp_set_col_name(mp.lp, index, "V");
		 	GLPK.glp_set_col_kind(mp.lp, index, GLPKConstants.GLP_IV);
		    GLPK.glp_set_col_bnds(mp.lp, index, GLPKConstants.GLP_DB, mp.minV, mp.maxV);
		 	index++;
		 	//System.out.println("3--");
			// contraintes
            int contIndex=1;
            
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					for(int j=0;j<mp.nbStages;j++){
						for(int r=0;r<mp.nbTezTasks[j];r++){
								for(int a=0;a<mp.nbTezSlots;a++){
									for(int b=0;b<mp.nbTezSlots;b++){
										if(mp.Q[i][j]>0){
											ind = GLPK.new_intArray(4);
		    	  				            val = GLPK.new_doubleArray(4);  
		    	  							GLPK.glp_add_rows(mp.lp, 1);
		    	  							GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
		    	  							GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,mp.Q[i][j], mp.Q[i][j]);
		    	  						    GLPK.intArray_setitem(ind, 1, mp.getXIndex(i,m,a));
		    	  						    GLPK.intArray_setitem(ind, 2, mp.getXIndex(j,r,b));
		    	  						    GLPK.intArray_setitem(ind, 3, mp.getZIndex(a,b));
		    	  						    GLPK.doubleArray_setitem(val, 1, mp.Q[i][j]);
		    	  						    GLPK.doubleArray_setitem(val, 2, mp.Q[i][j]);
		    	  						    GLPK.doubleArray_setitem(val, 3, -1);
		    	  						    GLPK.glp_set_mat_row(mp.lp, contIndex, 3, ind, val);
		    	  						    contIndex++;
	    	  						    }
									}
								}
							}
						}
					}
				}
			
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					for(int a=0;a<mp.nbTezSlots;a++){
						ind = GLPK.new_intArray(2);
				        val = GLPK.new_doubleArray(2);
				        GLPK.glp_add_rows(mp.lp, 1);
				        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
				        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,mp.PROCS[a], mp.PROCS[a]);
					    GLPK.intArray_setitem(ind, 1, mp.getXIndex(i,m,a));
					    GLPK.doubleArray_setitem(val, 1, mp.PROCT[i]);
					    GLPK.glp_set_mat_row(mp.lp, contIndex, 1, ind, val);
					    contIndex++;
					}
				}
			}
			
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					for(int a=0;a<mp.nbTezSlots;a++){
						ind = GLPK.new_intArray(2);
				        val = GLPK.new_doubleArray(2);
				        GLPK.glp_add_rows(mp.lp, 1);
				        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
				        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,mp.MEMS[a],mp.MEMS[a]);
					    GLPK.intArray_setitem(ind, 1, mp.getXIndex(i,m,a));
					    GLPK.doubleArray_setitem(val, 1, mp.MEMT[i]);
					    GLPK.glp_set_mat_row(mp.lp, contIndex, 1, ind, val);
					    contIndex++;
					}
				}
			}
			
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					ind = GLPK.new_intArray(mp.nbTezSlots+1);
			        val = GLPK.new_doubleArray(mp.nbTezSlots+1);
			        GLPK.glp_add_rows(mp.lp, 1);
			        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_FX,1 ,1);
			        for(int a=0;a<mp.nbTezSlots;a++){
			        	GLPK.intArray_setitem(ind, a+1, mp.getXIndex(i,m,a));
			        	GLPK.doubleArray_setitem(val, a+1, 1);
			        }
				    GLPK.glp_set_mat_row(mp.lp, contIndex, mp.nbTezSlots, ind, val);
				    contIndex++;
				}
			}	
			
			for(int i=0;i<mp.nbStages;i++){
				for(int a=0;a<mp.nbTezSlots;a++){
					ind = GLPK.new_intArray(mp.nbTezTasks[i]+1);
			        val = GLPK.new_doubleArray(mp.nbTezTasks[i]+1);
			        GLPK.glp_add_rows(mp.lp, 1);
			        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,1 ,1);
			        for(int m=0;m<mp.nbTezTasks[i];m++){
			        	GLPK.intArray_setitem(ind, m+1, mp.getXIndex(i,m,a));
			        	GLPK.doubleArray_setitem(val, m+1, 1);
			        }
				    GLPK.glp_set_mat_row(mp.lp, contIndex, mp.nbTezTasks[i], ind, val);
				    contIndex++;
				}
			}
			
			for(int a=0;a<mp.nbTezSlots;a++){	
				double somme=0;
				for(int t=0;t<mp.T;t++){
					somme+=(1-mp.F[a][t]);
				}
				ind = GLPK.new_intArray(mp.totalNbTezTasks+1+1);
		        val = GLPK.new_doubleArray(mp.totalNbTezTasks+1+1);
		        GLPK.glp_add_rows(mp.lp, 1);
		        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
		        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,-somme ,-somme);
		        int ii=1;
		        for(int i=0;i<mp.nbStages;i++){
		        	for(int j=0;j<mp.nbTezTasks[i];j++){
		        		GLPK.intArray_setitem(ind, ii, mp.getXIndex(i,j,a));
			        	GLPK.doubleArray_setitem(val, ii, mp.Dt[i]);
			        	ii++;
		        	}
		        }
        		GLPK.intArray_setitem(ind, ii, mp.getVIndex());
	        	GLPK.doubleArray_setitem(val, ii, -1);
	        	GLPK.glp_set_mat_row(mp.lp, contIndex,mp.totalNbTezTasks+1, ind, val);
			    contIndex++;
			}
						
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					for(int a=0;a<mp.nbTezSlots;a++){
						ind = GLPK.new_intArray(2);
				        val = GLPK.new_doubleArray(2);
				        GLPK.glp_add_rows(mp.lp, 1);
				        GLPK.glp_set_row_name(mp.lp, contIndex, "c"+contIndex);
				        GLPK.glp_set_row_bnds(mp.lp, contIndex, GLPKConstants.GLP_UP,mp.ACTS[a], mp.ACTS[a]);
					    GLPK.intArray_setitem(ind, 1, mp.getXIndex(i,m,a));
					    GLPK.doubleArray_setitem(val, 1, 1);
					    GLPK.glp_set_mat_row(mp.lp, contIndex, 1, ind, val);
					    contIndex++;
					}
				}
			}
			//System.out.println("4--");
			// fonction objective
			GLPK.glp_set_obj_coef(mp.lp, 0,mp.totalNbTezTasks*mp.PXnonPlacees);
			for(int m=0;m<mp.nbTezSlots;m++){
				for(int r=0;r<mp.nbTezSlots;r++){
					GLPK.glp_set_obj_coef(mp.lp, mp.getZIndex(m,r),mp.Pcomm*mp.DIST[m][r]);
				}
			}
			for(int i=0;i<mp.nbStages;i++){
				for(int m=0;m<mp.nbTezTasks[i];m++){
					for(int a=0;a<mp.nbTezSlots;a++){
						GLPK.glp_set_obj_coef(mp.lp, mp.getXIndex(i,m,a),mp.Dt[i]*(mp.Pproc*mp.PROCS[a]+mp.Pmem*mp.MEMS[a])-mp.PXnonPlacees);
					}
				}
			}
			GLPK.glp_set_obj_coef(mp.lp, mp.getVIndex(),mp.coefRepartition);
			

			GlpkCallback.addListener(mp);
            // Solve model
        	//System.out.println("5--");
        	iocp = new glp_iocp();
        	//System.out.println("1---");
        	GLPK.glp_init_iocp(iocp);
        	//System.out.println("2---");
        	iocp.setPresolve(GLPKConstants.GLP_ON);
        	//System.out.println("3---");
        	ret = GLPK.glp_intopt(mp.lp, iocp);
        	//System.out.println("6--");
            
            // Retrieve solution
            if (ret == 0 || mp.bonneSolutionTrouve) {
                //write_mip_solution(lp);
            	mp.setOutput(mp.lp);
            	mp.afficher(mp.lp);
            } else {
            	if(VariablesGlobales.verbose) System.out.println("The problem could not be solved");
            	mp.resolu=false;
            };
            GLPK.glp_delete_prob(mp.lp);
            //System.out.println("7--");
            //GLPK.glp_free_env();
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
		
		if(VariablesGlobales.verbose) System.out.println("Objectif Placement:"+GLPK.glp_mip_obj_val(lp));
		
		for(int i=0;i<nbStages;i++){
			for(int j=0;j<nbTezTasks[i];j++){
				if(VariablesGlobales.verbose) System.out.print("X["+i+"]["+j+"] = (");
				for(int a=0;a<this.nbTezSlots;a++){
					val  = GLPK.glp_mip_col_val(lp, getXIndex(i,j,a));
					if(VariablesGlobales.verbose) System.out.print((int)val+" ");
				}
				if(VariablesGlobales.verbose) System.out.println(")");
				if(VariablesGlobales.verbose) System.out.println("");
				
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
		if(VariablesGlobales.verbose) System.out.println("Nombre de taches non placees : "+nb+"/"+totalNbTezTasks);
	
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

		if(VariablesGlobales.verbose) System.out.println("Cout communication : "+coutTotalCommunication);
		if(VariablesGlobales.verbose) System.out.println("Cout processeur : "+coutTotalProcesseur);
		if(VariablesGlobales.verbose) System.out.println("Cout mémoire : "+coutTotalMemoire);
		if(VariablesGlobales.verbose) System.out.println("Cout Ressources "+(VariablesGlobales.Pproc*coutTotalProcesseur+VariablesGlobales.Pmem*coutTotalMemoire));
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
