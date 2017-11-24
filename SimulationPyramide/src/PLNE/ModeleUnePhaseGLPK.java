package PLNE;

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
import Entite.*;
import Gantt.Gantt;
import Gantt.TrancheTempsAlloue;


public class ModeleUnePhaseGLPK  implements GlpkCallbackListener{
	Cloud cloud;
	int nbJobs;
	int T;
	int[] nbMapTasks;
	int[]  nbReduceTasks;
	int nbMapSlots;
	int nbReduceSlots;
	glp_prob lp;
	double gap=-1;
	
	double[][] DIST;
	double[][] Q;
	double[] PROCMT;
	double[] PROCRT;
	double[] STORMT;
	double[] STORRT;
	double[] MEMMT;
	double[] MEMRT;
	double[] PROCMS;
	double[] PROCRS;
	double[] STORMS;
	double[] STORRS;
	double[] MEMMS;
	double[] MEMRS;
	//double[] tauxMinMap;
	//double[] tauxMinReduce;
	//double[] tauxMaxMap;
	//double[] tauxMaxReduce;
	int O[][];
	int Tm[];
	int Tr[];
	double P[];
	int D[];
	
	int Fm[][];  
	int Fr[][];
	
	double Pcomm,Pproc,Pmem,Pstor,Ppenalites,PXnonPlacees,PYnonPlacees,coefRepartition;
	
	int indexDebY;
	int indexDebZ;
	int indexDebV;
	int indexDebW;
	int indexDebR;
	double minVW=0;
	double maxVW=10000000;
	double minZ=0;
	double maxZ=10000000;
	double minR=0;
	double maxR=10000000;
	
	
	int totalNbMapTasks=0;
	int totalNbReduceTasks=0;
	
	boolean bonneSolutionTrouve=false;
	
	
	public ModeleUnePhaseGLPK(Cloud cloud){
		////////////////////////
		this.cloud=cloud;
		nbJobs=cloud.getNbJobs();
		nbMapSlots=VariablesGlobales.mapSlotsIndex;
		nbReduceSlots=VariablesGlobales.reduceSlotsIndex;
		Pcomm=VariablesGlobales.Pcomm;
		Pproc=VariablesGlobales.Pproc;
		Pmem=VariablesGlobales.Pmem;
		Pstor=VariablesGlobales.Pstor;
		PXnonPlacees=VariablesGlobales.PXnonPlacees;
		PYnonPlacees=VariablesGlobales.PYnonPlacees;
		coefRepartition=VariablesGlobales.coefRepartition;
		Ppenalites=VariablesGlobales.Ppenalites;
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
		
		DIST=new double[nbMapSlots][nbReduceSlots];
		Q=new double[nbJobs][nbJobs];
		PROCMT=new double[nbJobs];
		PROCRT=new double[nbJobs];
		STORMT=new double[nbJobs];
		STORRT=new double[nbJobs];
		MEMMT=new double[nbJobs];
		MEMRT=new double[nbJobs];
		PROCMS=new double[nbMapSlots];
		PROCRS=new double[nbReduceSlots];
		STORMS=new double[nbMapSlots];
		STORRS=new double[nbReduceSlots];
		MEMMS=new double[nbMapSlots];
		MEMRS=new double[nbReduceSlots];
		//tauxMinMap=new double[nbMapSlots];
		//tauxMinReduce=new double[nbReduceSlots];
		//tauxMaxMap=new double[nbMapSlots];
		//tauxMaxReduce=new double[nbReduceSlots];
		MEMRS=new double[nbReduceSlots];
		O=new int[nbJobs][nbJobs];
		
		Fm=new int[nbMapSlots][T];      
		Fr=new int[nbReduceSlots][T]; 
		
		P=new double[nbJobs];
		D=new int[nbJobs];
		Tm=new int[nbJobs];
		Tr=new int[nbJobs];
		
		///////////////////////
		for(int a=0;a<nbMapSlots;a++){
			for(int b=0;b<nbReduceSlots;b++){
				DIST[a][b]=cloud.getDistanceEntreSlots(a,b);
			}
		}
		
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbJobs;j++){
				O[i][j]=0;
				Q[i][j]=0;
			}
		}
		for(ClasseClients c : cloud.listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				for(Job job1 : r.listeJobs){
					for(Job job2 : r.listeJobs){
						O[job1.indexJob][job2.indexJob]=r.getDepandance(job1, job2);
						Q[job1.indexJob][job2.indexJob]=r.getQuantiteTransfertJobs(job1, job2);
					}
				}
			}
		}
		
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				for(int a=vm.indexDebutSlotsMap;a<vm.indexDebutSlotsMap+vm.nbMapSlots;a++){
					PROCMS[a]=vm.processeurMapSlots;
					MEMMS[a]=vm.memoireMapSlots;
					STORMS[a]=vm.stockageMapSlots;
				}
			}
		}
		
		for(MachinePhysique mp : cloud.listeMachinesPhysique){
			for(VM vm : mp.ListeVMs){
				for(int b=vm.indexDebutSlotsReduce;b<vm.indexDebutSlotsReduce+vm.nbReduceSlots;b++){
					PROCRS[b]=vm.processeurReduceSlots;
					MEMRS[b]=vm.memoireReduceSlots;
					STORRS[b]=vm.stockageReduceSlots;
				}
			}
		}
		
		for(ClasseClients c : cloud.listeClassesClient){
			for(Requete r : c.requeteEnAttente){
				for(Job job : r.listeJobs){
					PROCMT[job.indexJob]=job.processeurTacheMap;
					MEMMT[job.indexJob]=job.memoireTacheMap;
					STORMT[job.indexJob]=job.stockageTacheMap;
					PROCRT[job.indexJob]=job.processeurTacheReduce;
					MEMRT[job.indexJob]=job.memoireTacheReduce;
					STORRT[job.indexJob]=job.stockageTacheReduce;
				}
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
		
	
		
		
		/*for(int a=0;a<nbMapSlots;a++){
			tauxMinMap[a]=(double)0.5/(double)nbMapSlots;
			tauxMaxMap[a]=1;
		}
		
		for(int b=0;b<nbReduceSlots;b++){
			tauxMinReduce[b]=(double)0.5/(double)nbReduceSlots;
			tauxMaxReduce[b]=1;
		}*/
		
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
		
        ////////////////////////
		
		totalNbMapTasks=0;
		for(int i=0;i<nbJobs;i++){
			totalNbMapTasks=totalNbMapTasks+nbMapTasks[i];
		}
		
		totalNbReduceTasks=0;
		for(int i=0;i<nbJobs;i++){
			totalNbReduceTasks=totalNbReduceTasks+nbReduceTasks[i];
		}
	}
	
	public Gantt resoudre(){
        glp_iocp iocp;
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int ret;
        Gantt gantt=null;
        
		try{
            // Create problem
			
            lp = GLPK.glp_create_prob();
            System.out.println("Problem créé - 12/06 14:23 "+getYIndex(1, 2, 1, 1));
            GLPK.glp_set_prob_name(lp, "Probleme placement");
            
            // Define columns
            int index=1;
            for(int i=0;i<nbJobs;i++){
            	for(int j=0;j<nbMapTasks[i];j++){
            		for(int a=0;a<nbMapSlots;a++){
            			for(int t=0;t<T;t++){
		        			 GLPK.glp_add_cols(lp, 1);
		        			 GLPK.glp_set_col_name(lp, index, "X["+i+"]["+j+"]["+a+"]["+t+"]");
		        			 GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
		        			 //GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, 1);
		        			 index++;
            			}
            		}
            	}
            }
            
            indexDebY=index;
            for(int i=0;i<nbJobs;i++){
            	for(int j=0;j<nbReduceTasks[i];j++){
            		for(int b=0;b<nbReduceSlots;b++){
            			for(int t=0;t<T;t++){
	            			GLPK.glp_add_cols(lp, 1);
	           			 	GLPK.glp_set_col_name(lp, index, "Y["+i+"]["+j+"]["+b+"]["+t+"]");
	           			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_BV);
	           			    //GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, 0, 1);
	           			 	index++;
            			}
            		}
            	}
        	}
            
            
            indexDebZ=index;
            for(int m=0;m<nbMapSlots;m++){
            	for(int r=0;r<nbReduceSlots;r++){
            		GLPK.glp_add_cols(lp, 1);
            		GLPK.glp_set_col_name(lp, index, "Z["+m+"]["+r+"]");
       			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
       			    GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minZ, this.maxZ);
       			 	index++;
            	}
            }
            
            indexDebV=index;
            for(int a=0;a<nbMapSlots;a++){
        		GLPK.glp_add_cols(lp, 1);
        		GLPK.glp_set_col_name(lp, index, "V["+a+"]");
   			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
   			    GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minVW, this.maxVW);
   			 	index++;
            }
            
            indexDebW=index;
            for(int b=0;b<nbReduceSlots;b++){
        		GLPK.glp_add_cols(lp, 1);
        		GLPK.glp_set_col_name(lp, index, "W["+b+"]");
   			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
   			    GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minVW, this.maxVW);
   			 	index++;
            }
            
            
            indexDebR=index;
            for(int i=0;i<nbJobs;i++){
            	for(int t=0;t<T;t++){
            		GLPK.glp_add_cols(lp, 1);
       			 	GLPK.glp_set_col_name(lp, index, "R["+i+"]["+t+"]");
       			 	GLPK.glp_set_col_kind(lp, index, GLPKConstants.GLP_IV);
       			 GLPK.glp_set_col_bnds(lp, index, GLPKConstants.GLP_DB, this.minR, this.maxR);
            		index++;
            	}
        	}
            
            // contraintes
            int contIndex=1;
            
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					for(int j=0;j<nbJobs;j++){
						for(int r=0;r<nbReduceTasks[j];r++){
							if(Q[i][j]>0){
								for(int a=0;a<nbMapSlots;a++){
									for(int b=0;b<nbReduceSlots;b++){
										ind = GLPK.new_intArray(4);
	    	  				            val = GLPK.new_doubleArray(4);  
	    	  							GLPK.glp_add_rows(lp, 1);
	    	  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
	    	  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,Q[i][j], Q[i][j]);
	    	  						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a,T-1));
	    	  						    GLPK.intArray_setitem(ind, 2, getYIndex(j,r,b,T-1));
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
			
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					for(int a=0;a<nbMapSlots;a++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,PROCMS[a], PROCMS[a]);
						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a,t));
						    GLPK.doubleArray_setitem(val, 1, PROCMT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
				
				for(int r=0;r<nbReduceTasks[i];r++){
					for(int b=0;b<nbReduceSlots;b++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,PROCRS[b], PROCRS[b]);
						    GLPK.intArray_setitem(ind, 1, getYIndex(i,r,b,t));
						    GLPK.doubleArray_setitem(val, 1, PROCRT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
			}
			
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					for(int a=0;a<nbMapSlots;a++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,MEMMS[a],MEMMS[a]);
						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a,t));
						    GLPK.doubleArray_setitem(val, 1, MEMMT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
				
				for(int r=0;r<nbReduceTasks[i];r++){
					for(int b=0;b<nbReduceSlots;b++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,MEMRS[b] ,MEMRS[b]);
						    GLPK.intArray_setitem(ind, 1, getYIndex(i,r,b,t));
						    GLPK.doubleArray_setitem(val, 1, MEMRT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
			}
			
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					for(int a=0;a<nbMapSlots;a++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,STORMS[a] ,STORMS[a]);
						    GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a,t));
						    GLPK.doubleArray_setitem(val, 1, STORMT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
				
				for(int r=0;r<nbReduceTasks[i];r++){
					for(int b=0;b<nbReduceSlots;b++){
						for(int t=0;t<T;t++){
							ind = GLPK.new_intArray(2);
					        val = GLPK.new_doubleArray(2);
					        GLPK.glp_add_rows(lp, 1);
					        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,STORRS[b] ,STORRS[b]);
						    GLPK.intArray_setitem(ind, 1, getYIndex(i,r,b,t));
						    GLPK.doubleArray_setitem(val, 1, STORRT[i]);
						    GLPK.glp_set_mat_row(lp, contIndex, 1, ind, val);
						    contIndex++;
						}
					}
				}
			}
						
			
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					ind = GLPK.new_intArray(nbMapSlots+1);
			        val = GLPK.new_doubleArray(nbMapSlots+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_FX,1 ,1);
			        for(int a=0;a<nbMapSlots;a++){
			        	GLPK.intArray_setitem(ind, a+1, getXIndex(i,m,a,T-1));
			        	GLPK.doubleArray_setitem(val, a+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbMapSlots, ind, val);
				    contIndex++;
				}
			}
			
			for(int i=0;i<nbJobs;i++){
				for(int r=0;r<nbReduceTasks[i];r++){
					ind = GLPK.new_intArray(nbReduceSlots+1);
			        val = GLPK.new_doubleArray(nbReduceSlots+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_FX,1 ,1);
			        for(int b=0;b<nbReduceSlots;b++){
			        	GLPK.intArray_setitem(ind, b+1, getYIndex(i,r,b,T-1));
			        	GLPK.doubleArray_setitem(val, b+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbReduceSlots, ind, val);
				    contIndex++;
				}
			}
			
			
			
			for(int i=0;i<nbJobs;i++){
				for(int a=0;a<nbMapSlots;a++){
					ind = GLPK.new_intArray(nbMapTasks[i]+1);
			        val = GLPK.new_doubleArray(nbMapTasks[i]+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,1 ,1);
			        for(int m=0;m<nbMapTasks[i];m++){
			        	GLPK.intArray_setitem(ind, m+1, getXIndex(i,m,a,T-1));
			        	GLPK.doubleArray_setitem(val, m+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbMapTasks[i], ind, val);
				    contIndex++;
				}
			}
			
			
			for(int i=0;i<nbJobs;i++){
				for(int b=0;b<nbReduceSlots;b++){
					ind = GLPK.new_intArray(nbReduceTasks[i]+1);
			        val = GLPK.new_doubleArray(nbReduceTasks[i]+1);
			        GLPK.glp_add_rows(lp, 1);
			        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,1 ,1);
			        for(int r=0;r<nbReduceTasks[i];r++){
			        	GLPK.intArray_setitem(ind, r+1, getYIndex(i,r,b,T-1));
			        	GLPK.doubleArray_setitem(val, r+1, 1);
			        }
				    GLPK.glp_set_mat_row(lp, contIndex, nbReduceTasks[i], ind, val);
				    contIndex++;
				}
			}
			
			for(int a=0;a<nbMapSlots;a++){	
				double somme=0;
				for(int t=0;t<T;t++){
					somme+=Fm[a][t];
				}
				ind = GLPK.new_intArray(totalNbMapTasks+1+1);
		        val = GLPK.new_doubleArray(totalNbMapTasks+1+1);
		        GLPK.glp_add_rows(lp, 1);
		        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
		        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,-somme ,-somme);
		        int ii=1;
		        for(int i=0;i<nbJobs;i++){
		        	for(int j=0;j<nbMapTasks[i];j++){
		        		GLPK.intArray_setitem(ind, ii, getXIndex(i,j,a,T-1));
			        	GLPK.doubleArray_setitem(val, ii, Tm[i]);
			        	ii++;
		        	}
		        }
        		GLPK.intArray_setitem(ind, ii, getVIndex(a));
	        	GLPK.doubleArray_setitem(val, ii, -1);
	        	GLPK.glp_set_mat_row(lp, contIndex,totalNbMapTasks+1, ind, val);
			    contIndex++;
			}
			
			for(int b=0;b<nbReduceSlots;b++){
				double somme=0;
				for(int t=0;t<T;t++){
					somme+=Fr[b][t];
				}
				ind = GLPK.new_intArray(totalNbReduceTasks+1+1);
		        val = GLPK.new_doubleArray(totalNbReduceTasks+1+1);
		        GLPK.glp_add_rows(lp, 1);
		        GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
		        GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP,-somme ,-somme);
		        int ii=1;
		        for(int i=0;i<nbJobs;i++){
		        	for(int j=0;j<nbReduceTasks[i];j++){
		        		GLPK.intArray_setitem(ind, ii, getYIndex(i,j,b,T-1));
			        	GLPK.doubleArray_setitem(val, ii, Tr[i]);
			        	ii++;
		        	}
		        }
        		GLPK.intArray_setitem(ind, ii, getWIndex(b));
	        	GLPK.doubleArray_setitem(val, ii, -1);
	        	GLPK.glp_set_mat_row(lp, contIndex,totalNbReduceTasks+1, ind, val);
			    contIndex++;
			}
			
			
			//////// Ordonnancement
			
			ind = GLPK.new_intArray(nbReduceSlots+2);
            val = GLPK.new_doubleArray(nbReduceSlots+2);
            for(int i=0;i<nbJobs;i++){
				for(int t=0;t<T;t++){				
					for(int r=0;r<nbReduceTasks[i];r++){
						GLPK.glp_add_rows(lp, 1);
						GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
						GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, nbReduceSlots, nbReduceSlots);
						int indexx=1;
						for(int b=0;b<nbReduceSlots;b++){
							GLPK.intArray_setitem(ind, indexx, getYIndex(i,r,b,t));
							GLPK.doubleArray_setitem(val, indexx, 1);
							indexx++;
						}
						GLPK.intArray_setitem(ind, indexx, getRIndex(i,t));
			            GLPK.doubleArray_setitem(val, indexx, 1);
			            GLPK.glp_set_mat_row(lp, contIndex, nbReduceSlots+1, ind, val);
						contIndex++;
					}
				}
            }
			
            for(int i=0;i<nbJobs;i++){
    			  for(int m=0;m<nbMapTasks[i];m++){
    				  for(int a=0;a<nbMapSlots;a++){
	    				  for(int t=0;t<T;t++){
	    					  if(t<T-1){
	    						  ind = GLPK.new_intArray(3);
	    				          val = GLPK.new_doubleArray(3);
	    						  GLPK.glp_add_rows(lp, 1);
	    						  GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
	    						  GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, 0, 0);
	    						  GLPK.intArray_setitem(ind, 1, getXIndex(i,m,a,t));
	    						  GLPK.intArray_setitem(ind, 2, getXIndex(i,m,a,t+1));
	    						  GLPK.doubleArray_setitem(val, 1, -1);
	    						  GLPK.doubleArray_setitem(val, 2, 1);
	    						  GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
	    						  contIndex++;
	    					  }
	    				  }
    				  }
    			  }
    		  }
            
            for(int i=0;i<nbJobs;i++){
  			  for(int r=0;r<nbReduceTasks[i];r++){
  				for(int b=0;b<nbReduceSlots;b++){
  				  for(int t=0;t<T;t++){
  					  if(t<T-1){
						  ind = GLPK.new_intArray(3);
				          val = GLPK.new_doubleArray(3);
  						  GLPK.glp_add_rows(lp, 1);
  						  GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
  						  GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_LO, 0, 0);
  						  GLPK.intArray_setitem(ind, 1, getYIndex(i,r,b,t));
  						  GLPK.intArray_setitem(ind, 2, getYIndex(i,r,b,t+1));
  						  GLPK.doubleArray_setitem(val, 1, -1);
  						  GLPK.doubleArray_setitem(val, 2, 1);
  						  GLPK.glp_set_mat_row(lp, contIndex, 2, ind, val);
  						  contIndex++;
  					  }
  				  }
  				}
  			  }
          }

          for(int i=0;i<nbJobs;i++){
			  for(int m=0;m<nbMapTasks[i];m++){
				  for(int r=0;r<nbReduceTasks[i];r++){
						  for(int t=0;t<T;t++){
							  if(t-Tm[i]>=1){
					            ind = GLPK.new_intArray(nbMapSlots+nbReduceSlots+1);
					            val = GLPK.new_doubleArray(nbMapSlots+nbReduceSlots+1);  
								GLPK.glp_add_rows(lp, 1);
								GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
								GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 0, 0);
								int indexx=1;
								for(int a=0;a<nbMapSlots;a++){
									GLPK.intArray_setitem(ind, indexx, getXIndex(i,m,a,t-Tm[i]));
									GLPK.doubleArray_setitem(val, indexx, -1);
									indexx++;
								}
								for(int b=0;b<nbReduceSlots;b++){
									GLPK.intArray_setitem(ind, indexx, getYIndex(i,r,b,t));
									GLPK.doubleArray_setitem(val, indexx, 1);
									indexx++;
								}
							    GLPK.glp_set_mat_row(lp, contIndex, nbMapSlots+nbReduceSlots, ind, val);
							    contIndex++;
						      }
							  else if(t-Tm[i]<1){
					            ind = GLPK.new_intArray(nbReduceSlots+1);
					            val = GLPK.new_doubleArray(nbReduceSlots+1);
					            GLPK.glp_add_rows(lp, 1);
					            GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
					            GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 0, 0);
					            int indexx=1;
					            for(int b=0;b<nbReduceSlots;b++){
					            	GLPK.intArray_setitem(ind, indexx, getYIndex(i,r,b,t));
					            	GLPK.doubleArray_setitem(val, indexx, 1);
					            	indexx++;
					            }
					            GLPK.glp_set_mat_row(lp, contIndex, nbReduceSlots, ind, val);
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
  						if(t-Tm[i]>=1){
  							nbElement+=2;
  						}
  						else if(t-Tm[i]<1){
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
  						if(t-Tm[i]>=1){
						    GLPK.intArray_setitem(ind, iii, getXIndex(i,m,k,t));
						    GLPK.doubleArray_setitem(val, iii, 1);
						    iii++;
						    GLPK.intArray_setitem(ind, iii, getXIndex(i,m,k,t-Tm[i]));
  						    GLPK.doubleArray_setitem(val, iii, -1);
  						    iii++;
  						}
  						else if(t-Tm[i]<1){
  							GLPK.intArray_setitem(ind, iii, getXIndex(i,m,k,t));
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
  						if(t-Tr[i]>=1){
  							nbElement+=2;
  						}
  						else if(t-Tr[i]<1){
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
  						if(t-Tr[i]>=1){
    						    GLPK.intArray_setitem(ind, iii, getYIndex(i,r,k,t));
    						    GLPK.doubleArray_setitem(val, iii, 1);
    						    iii++;
    						    GLPK.intArray_setitem(ind, iii, getYIndex(i,r,k,t-Tr[i]));
  						    GLPK.doubleArray_setitem(val, iii, -1);
  						    iii++;
  						}
  						else if(t-Tr[i]<1){
  							GLPK.intArray_setitem(ind, iii, getYIndex(i,r,k,t));
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
		  							    ind = GLPK.new_intArray(nbMapSlots+nbReduceSlots+1);
		  	  				            val = GLPK.new_doubleArray(nbMapSlots+nbReduceSlots+1);  
		  	  							GLPK.glp_add_rows(lp, 1);
		  	  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
		  	  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 1-O[i][j], 1-O[i][j]);
		  	  							int indexx=1;
		  	  							for(int a=0;a<nbMapSlots;a++){
		  	  								GLPK.intArray_setitem(ind, indexx, getXIndex(j,m,a,t));
		  	  								GLPK.doubleArray_setitem(val, indexx, 1);
		  	  								indexx++;
		  	  							}
		  	  							for(int b=0;b<nbReduceSlots;b++){
		  	  								GLPK.intArray_setitem(ind, indexx, getYIndex(i,r,b,t-Tr[i]));
		  	  								GLPK.doubleArray_setitem(val, indexx, -1);
		  	  								indexx++;
		  	  							}
		  	  						    GLPK.glp_set_mat_row(lp, contIndex, nbMapSlots+nbReduceSlots, ind, val);
		  	  						    contIndex++;
		  						  }
		  						  else if(i!=j && t-Tr[i]<1){
		  							    ind = GLPK.new_intArray(nbMapSlots+1);
			  				            val = GLPK.new_doubleArray(nbMapSlots+1);  
			  							GLPK.glp_add_rows(lp, 1);
			  							GLPK.glp_set_row_name(lp, contIndex, "c"+contIndex);
			  							GLPK.glp_set_row_bnds(lp, contIndex, GLPKConstants.GLP_UP, 1-O[i][j], 1-O[i][j]);
			  							int indexx=1;
		  	  							for(int a=0;a<nbMapSlots;a++){
			  								GLPK.intArray_setitem(ind, indexx, getXIndex(j,m,a,t));
			  								GLPK.doubleArray_setitem(val, indexx, 1);
			  								indexx++;
		  	  							}
			  							GLPK.glp_set_mat_row(lp, contIndex, nbMapSlots, ind, val);
			  						    contIndex++;
		  						  }
		  					  }
  						}
  			  }
  			}
  		}
          
			// fonction objective
            double coef0=totalNbMapTasks*PXnonPlacees+totalNbReduceTasks*PYnonPlacees;
            for(int i=0;i<nbJobs;i++){
				for(int t=0;t<T;t++){
					if(t>D[i]-Tr[i]){
						coef0-=Ppenalites*P[i]*(nbReduceSlots-1);
					}
				}
            }
            
			GLPK.glp_set_obj_coef(lp, 0,coef0);
			for(int m=0;m<nbMapSlots;m++){
				for(int r=0;r<nbReduceSlots;r++){
					GLPK.glp_set_obj_coef(lp, getZIndex(m,r),Pcomm*DIST[m][r]);
				}
			}
			for(int i=0;i<nbJobs;i++){
				for(int m=0;m<nbMapTasks[i];m++){
					for(int a=0;a<nbMapSlots;a++){
						GLPK.glp_set_obj_coef(lp, getXIndex(i,m,a,T-1),Tm[i]*(Pproc*PROCMS[a]+Pmem*MEMMS[a]+Pstor*STORMS[a])-PXnonPlacees);
					}
				}
			}
			for(int i=0;i<nbJobs;i++){
				for(int r=0;r<nbReduceTasks[i];r++){
					for(int b=0;b<nbReduceSlots;b++){
						GLPK.glp_set_obj_coef(lp, getYIndex(i,r,b,T-1),Tr[i]*(Pproc*PROCRS[b]+Pmem*MEMRS[b]+Pstor*STORRS[b])-PYnonPlacees);
					}
				}
			}
			for(int a=0;a<nbMapSlots;a++){
				GLPK.glp_set_obj_coef(lp, getVIndex(a),this.coefRepartition);
			}
			for(int b=0;b<nbReduceSlots;b++){
				GLPK.glp_set_obj_coef(lp, getWIndex(b),this.coefRepartition);
			}
			
			index=indexDebR;
            for(int i=0;i<nbJobs;i++){
				for(int t=0;t<T;t++){
					if(t>D[i]-Tr[i]){
						GLPK.glp_set_obj_coef(lp, index, Ppenalites*P[i]);
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
            if (ret == 0 || this.bonneSolutionTrouve) {
                //write_mip_solution(lp);
            	//this.setOutput(lp);
            	//afficher(lp);
            	gantt=ecrireResultat(lp);
            } else {
                System.out.println("The problem could not be solved");
            };
            GLPK.glp_delete_prob(lp);
		}
		catch(GlpkException ex){
            ex.printStackTrace();
            ret = 1;
		}
		
		 return gantt;
	}
	
	public void afficher(glp_prob lp){
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

		System.out.println("Objectif Placement:"+GLPK.glp_mip_obj_val(lp));
		
		/*for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				System.out.print("X["+i+"]["+j+"] = (");
				for(int a=0;a<this.nbMapSlots;a++){
					val  = GLPK.glp_mip_col_val(lp, getXIndex(i,j,a));
		            System.out.print((int)val+" ");
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}
		
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbReduceTasks[i];j++){
				System.out.print("Y["+i+"]["+j+"] = (");
				for(int b=0;b<this.nbReduceSlots;b++){
					val  = GLPK.glp_mip_col_val(lp, getYIndex(i,j,b));
		            System.out.print((int)val+" ");
				}
				System.out.println(")");
				System.out.println("");
				
			}
		}*/
		
		/*for(int a=0;a<this.nbMapSlots;a++){
			for(int b=0;b<this.nbReduceSlots;b++){
				System.out.println("Z["+a+"]["+b+"] = "+GLPK.glp_mip_col_val(lp, getZIndex(a,b)));				
			}
		}*/
		
		/*int nb=totalNbMapTasks;
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				for(int a=0;a<nbMapSlots;a++){
					nb-=GLPK.glp_mip_col_val(lp, getXIndex(i,j,a));
				}
			}
		}
		System.out.println("Nombre de taches Map non placees : "+nb+"/"+totalNbMapTasks);
		nb=totalNbReduceTasks;
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbReduceTasks[i];j++){
				for(int a=0;a<nbReduceSlots;a++){
					nb-=GLPK.glp_mip_col_val(lp, getYIndex(i,j,a));
				}
			}
		}
		System.out.println("Nombre de taches Reduce non placees : "+nb+"/"+totalNbReduceTasks);*/
	
		int coutTotalCommunication=0;
		for(int m=0;m<nbMapSlots;m++){
			for(int r=0;r<nbReduceSlots;r++){
				coutTotalCommunication+=GLPK.glp_mip_col_val(lp, getZIndex(m,r))*DIST[m][r];
			}
		}
		
		int coutTotalProcesseur=0;
		int coutTotalMemoire=0;
		int coutTotalStockage=0;
		for(int i=0;i<nbJobs;i++){
			for(int m=0;m<nbMapTasks[i];m++){
				for(int a=0;a<nbMapSlots;a++){
					coutTotalProcesseur+=GLPK.glp_mip_col_val(lp, getXIndex(i,m,a,T-1))*Tm[i]*PROCMS[a];
					coutTotalMemoire+=GLPK.glp_mip_col_val(lp, getXIndex(i,m,a,T-1))*Tm[i]*MEMMS[a];
					coutTotalStockage+=GLPK.glp_mip_col_val(lp, getXIndex(i,m,a,T-1))*Tm[i]*STORMS[a];
				}
			}
		}
		for(int i=0;i<nbJobs;i++){
			for(int r=0;r<nbReduceTasks[i];r++){
				for(int b=0;b<nbReduceSlots;b++){
					coutTotalProcesseur+=GLPK.glp_mip_col_val(lp, getYIndex(i,r,b,T-1))*Tr[i]*PROCRS[b];
					coutTotalMemoire+=GLPK.glp_mip_col_val(lp, getYIndex(i,r,b,T-1))*Tr[i]*MEMRS[b];
					coutTotalStockage+=GLPK.glp_mip_col_val(lp, getYIndex(i,r,b,T-1))*Tr[i]*STORRS[b];
				}
			}
		}
		
		System.out.println("Cout communication : "+coutTotalCommunication);
		System.out.println("Cout processeur : "+coutTotalProcesseur);
		System.out.println("Cout mémoire : "+coutTotalMemoire);
		System.out.println("Cout stockage : "+coutTotalStockage);
	}
	
	
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
		
		System.out.println("Coût total :"+GLPK.glp_mip_obj_val(lp));
		int index=1;
		System.out.println("X = ");
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				for(int a=0;a<nbMapSlots;a++){
					System.out.print(a+" (");
					for(int t=0;t<T;t++){
						 System.out.print(GLPK.glp_mip_col_val(lp, getXIndex(i, j, a, t))+" ");
					}
					System.out.println(")");
				}			
			}
		}
		
		
	    int debut,fin;
	    Gantt gantt=new Gantt();
	    
		int Am[][];
		int Ar[][];
		
		Am=new int[nbJobs][];
		for(int i=0;i<nbJobs;i++)
		Am[i]=new int[nbMapTasks[i]];
		Ar=new int[nbJobs][];
		for(int i=0;i<nbJobs;i++)
		Ar[i]=new int[nbReduceTasks[i]];
		
		for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				Am[i][j]=0;
			}
			for(int j=0;j<nbReduceTasks[i];j++){
				Ar[i][j]=0;
			}
		}
	    
	    for(int i=0;i<nbJobs;i++){
			for(int j=0;j<nbMapTasks[i];j++){
				debut=0;
				fin=T-1;
				for(int t=0;t<T-1;t++){
					for(int a=0;a<nbMapSlots;a++){
						if(t-Tm[i]>=0){
							if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t-Tm[i])))< (GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t+1))-GLPK.glp_mip_col_val(lp,getXIndex(i,j,a,t+1-Tm[i])))){
								debut=t+1;
								Am[i][j]=a;
							}	
							else if((GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t-Tm[i])))>(GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t+1))-GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t+1-Tm[i]))) )
								fin=t;
						}
						else{
							if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t))<GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t+1))){
								debut=t+1;
								Am[i][j]=a;
							}
							else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t))>GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,t+1)))
								fin=t;
							else if(GLPK.glp_mip_col_val(lp, getXIndex(i,j,a,0))==1){
								debut=1;
								fin=Tm[i];
								Am[i][j]=a;
							}
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
					for(int b=0;b<nbReduceSlots;b++){
						if(t-Tr[i]>=0){
							if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t-Tr[i])))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1-Tr[i])))){
								debut=t+1;
								Ar[i][j]=b;
							}
							else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t-Tr[i])))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1))-GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1-Tr[i]))))
								fin=t;
						}
						else{
							if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t)))<(GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1)))){
								debut=t+1;
								Ar[i][j]=b;
							}
							else if((GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t)))>(GLPK.glp_mip_col_val(lp, getYIndex(i,j,b,t+1))))
								fin=t;
						}
					}
				}
				gantt.ajouterTrancheTemps(new TrancheTempsAlloue(2, Ar[i][j], Req[i], i, j, debut, fin));
			}
		}
	    
	    return gantt;
	}
	
	public int getXIndex(int ii, int jj,int aa,int tt){
		int index=0;
        for(int i=0;i<nbJobs;i++){
        	if(i<ii){
        		index+=nbMapTasks[i]*this.nbMapSlots*this.T;
        	}
        	else{
        		for(int j=0;j<nbMapTasks[i];j++){
        			if(j<jj){
        				index+=this.nbMapSlots*this.T;
        			}
        			else{
        				for(int a=0;a<this.nbMapSlots;a++){
        					if(a<aa){
        						index+=this.T;
        					}
        					else{
        						index+=(tt+1);
        						break;
        					}
        				}
        				break;
        			}
        		}
        		break;
        	}
        }
        return index;
	}
	
	public int getYIndex(int ii, int jj,int bb,int tt){
		int index= this.indexDebY-1;
        for(int i=0;i<nbJobs;i++){
        	if(i<ii){
        		index+=nbReduceTasks[i]*this.nbReduceSlots*this.T;
        	}
        	else{
        		for(int j=0;j<nbReduceTasks[i];j++){
        			if(j<jj){
        				index+=this.nbReduceSlots*this.T;
        			}
        			else{
        				for(int b=0;b<this.nbReduceSlots;b++){
        					if(b<bb){
        						index+=this.T;
        					}
        					else{
        						index+=(tt+1);
        						break;
        					}
        				}
        				break;
        			}
        		}
        		break;
        	}
        }
        return index;
	}
	
	public int getZIndex(int ii, int jj){
        return this.indexDebZ+ii*nbReduceSlots+jj;
	}
	
	public int getVIndex(int a){
        return this.indexDebV+a;
	}
	
	public int getWIndex(int b){
        return this.indexDebW+b;
	}
	
	public int getRIndex(int ii, int tt){
        return this.indexDebR+ii*T+tt;
	}
	
	public double coutPenalites(){
		double cout=0;
        for(int i=0;i<nbJobs;i++){
			for(int t=0;t<T;t++){
				if(t>D[i]-Tr[i]){
					cout=cout+P[i]*GLPK.glp_mip_col_val(lp, getZIndex(i, t));
				}
			}
        }
        
        return cout;
	}

	@Override
	public void callback(glp_tree arg0) {
		// TODO Auto-generated method stub
		int reason = GLPK.glp_ios_reason(arg0);	
		
        if (reason == GLPKConstants.GLP_IBINGO || reason==GLPKConstants.GLP_IHEUR) {
            if(GLPK.glp_ios_mip_gap(arg0) <= 0.1){
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
