package accounts.plugin.model.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import accounts.plugin.model.patti.Patti;

public class ModelManager {
	private static final ModelManager instance = new ModelManager();
	private Accounts model;
	private Patti pattiModel;
	private Map<String, String> balanceModel = new HashMap<>();

	private Map<Member, List<ItemBought>> mapOfItemsBt = new HashMap<>();
	private Map<Member, List<ItemSold>> mapOfItemsSold = new HashMap<>();

	public static ModelManager getInstance() {
		return instance;
	}

	public Accounts getModel() {
		return this.model;
	}

	public Patti getPattiModel() {
		return this.pattiModel;
	}

	public void setPattiModel(Patti model) {
		this.pattiModel = model;
	}

	public void setModel(Accounts model) {
		this.model = model;
	}

	public Map<Member, List<ItemBought>> getMapOfItemsBt() {
		return mapOfItemsBt;
	}

	public Map<Member, List<ItemSold>> getMapOfItemsSold() {
		return mapOfItemsSold;
	}

	public Map<String, String> getBalanceModel() {
		return this.balanceModel;
	}

	public Accounts loadModelFromSetFile(String filePath) {
		Accounts account = null;
		try {
			XmlDocument testSetData = new XmlDocument(null);
			testSetData.load(new File(filePath));
			Node accountsNode = testSetData.getNode(null, "Accounts", null);
			String accountName = XmlDocument.getAttributeValue(accountsNode, "Name");
			account = new Accounts(accountName);
			account.setName(accountName);
			Node membersNode = testSetData.getNode(accountsNode, "Members", null);
			for (Node memberNode : XmlDocument.getElements(membersNode, "Member", null)) {
				String memberName = XmlDocument.getAttributeValue(memberNode, "Name");
				Member member = new Member(memberName);

				Node yearsNode = testSetData.getNode(memberNode, "Years", null);
				Node monthsNode = testSetData.getNode(memberNode, "Months", null);
				if (yearsNode == null) {
					Year year = new Year("2017_18");
					member.getYears().add(year);
					loadDates(testSetData, monthsNode, year, member);
				} else {
					
					for (Node yearNode : XmlDocument.getElements(yearsNode, "Year", null)) {
						String yearName = XmlDocument.getAttributeValue(yearNode, "Name");
						Year year = new Year(yearName);
						member.getYears().add(year);
						monthsNode = testSetData.getNode(yearNode, "Months", null);
						loadDates(testSetData, monthsNode, year, member);
//						for (Node monthNode : XmlDocument.getElements(monthsNode, "Month", null)) {
//							String monthName = XmlDocument.getAttributeValue(monthNode, "Name");
//							Month month = new Month(monthName);
//							year.getMonths().add(month);
//							datesNode = testSetData.getNode(monthNode, "Dates", null);
//							loadDates(testSetData, monthsNode, year, member);
//						}
					}
				}

				account.getMembers().add(member);
			}
			setModel(account);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return account;
	}

	private void loadDates(XmlDocument testSetData, Node monthsNode, Year year, Member member) {
		for (Node monthNode : XmlDocument.getElements(monthsNode, "Month", null)) {
			String monthName = XmlDocument.getAttributeValue(monthNode, "Name");
			Month month = new Month(monthName);
			year.getMonths().add(month);
			for (Node datesNode : XmlDocument.getElements(monthNode, "Dates", null)) {
				for (Node dateNode : XmlDocument.getElements(datesNode, "Date", null)) {
					String dateName = XmlDocument.getAttributeValue(dateNode, "Name");
					Date date = new Date(dateName);
					month.getDates().add(date);
					for (Node itemBoughtNode : XmlDocument.getElements(dateNode, "ItemBought", null)) {
						String itemName = XmlDocument.getAttributeValue(itemBoughtNode, "Name");
						String vendorName = XmlDocument.getAttributeValue(itemBoughtNode, "Vendor");
						String noOfPockets = XmlDocument.getAttributeValue(itemBoughtNode, "NoOfPockets");
						String totalInKgs = XmlDocument.getAttributeValue(itemBoughtNode, "TotalInKGs");
						String ratePerKg = XmlDocument.getAttributeValue(itemBoughtNode, "RatePerKG");
						String miss = XmlDocument.getAttributeValue(itemBoughtNode, "Miscellaneous");
						String unloadingCharges = XmlDocument.getAttributeValue(itemBoughtNode, "UnloadingCharges");
						ItemBought itemBought = new ItemBought(itemName, vendorName, noOfPockets, totalInKgs, ratePerKg, miss,
								unloadingCharges, member.getName());
						List<ItemBought> list = mapOfItemsBt.get(member);
						if (list == null) {
							List<ItemBought> listOfItmsBt = new ArrayList();
							listOfItmsBt.add(itemBought);
							mapOfItemsBt.put(member, listOfItmsBt);
						} else {
							list.add(itemBought);
						}
						date.getItemsBought().add(itemBought);
						Node itemsSoldNode = testSetData.getNode(itemBoughtNode, "ItemsSold", null);
						for (Node itemSoldNode : XmlDocument.getElements(itemsSoldNode, "ItemSold", null)) {
							String personName = XmlDocument.getAttributeValue(itemSoldNode, "Name");
							String noOfPacks = XmlDocument.getAttributeValue(itemSoldNode, "NumberOfPacks");
							String kg = XmlDocument.getAttributeValue(itemSoldNode, "Kilograms");
							String rate = XmlDocument.getAttributeValue(itemSoldNode, "Rate");
							String transPortMiscExpense = XmlDocument.getAttributeValue(itemSoldNode,
									"TransportAndMiscExpenses");
							String totalPrice = XmlDocument.getAttributeValue(itemSoldNode, "TotalPrice");
							String previousBalance = XmlDocument.getAttributeValue(itemSoldNode, "PreviousBalance");
							String amtRec = XmlDocument.getAttributeValue(itemSoldNode, "AmountReceived");
							String amtbal = XmlDocument.getAttributeValue(itemSoldNode, "AmountBalance");
							String amtRecMode = XmlDocument.getAttributeValue(itemSoldNode, "AmountRecMode");
							String underMem = XmlDocument.getAttributeValue(itemSoldNode, "UnderMember");
							String billNo = XmlDocument.getAttributeValue(itemSoldNode, "BillNo");
							
							ItemSold itemSold = new ItemSold(personName, noOfPacks, kg, rate, transPortMiscExpense, totalPrice,
									previousBalance, amtRec, amtbal, amtRecMode, underMem, billNo);
							itemBought.getItemsSold().add(itemSold);
							
							List<ItemSold> list2 = mapOfItemsSold.get(member);
							if (list2 == null) {
								if (itemSold.getAmtRecMode().startsWith("CASH") || itemSold.getAmtRecMode().startsWith("ACC")) {
									List<ItemSold> listOfItmsSold = new ArrayList();
									listOfItmsSold.add(itemSold);
									mapOfItemsSold.put(member, listOfItmsSold);
								}
							} else {
								if (itemSold.getAmtRecMode().startsWith("CASH") || itemSold.getAmtRecMode().startsWith("ACC")) {
									list2.add(itemSold);
								}
							}
						}
					}
				}
				
				
			}
		}
	}

	public Patti loadPattiModelFromSetFile(String filePath) {
		Patti patti = null;
		try {
			XmlDocument testSetData = new XmlDocument(null);
			testSetData.load(new File(filePath));
			Node accountsNode = testSetData.getNode(null, "Patti", null);
			String accountName = XmlDocument.getAttributeValue(accountsNode, "Name");
			patti = new Patti(accountName);
			patti.setName(accountName);
			Node membersNode = testSetData.getNode(accountsNode, "Members", null);
			for (Node memberNode : XmlDocument.getElements(membersNode, "Member", null)) {
				String memberName = XmlDocument.getAttributeValue(memberNode, "Name");
				Member member = new Member(memberName);

				Node yearsNode = testSetData.getNode(memberNode, "Years", null);
				Node monthsNode = testSetData.getNode(memberNode, "Months", null);

				if (yearsNode == null) {
					Year year = new Year("2017_18");
					member.getYears().add(year);
					for (Node monthNode : XmlDocument.getElements(monthsNode, "Months", null)) {
						String monthName = XmlDocument.getAttributeValue(monthNode, "Name");
						Month month = new Month(monthName);
						year.getMonths().add(month);
						for (Node datesNode : XmlDocument.getElements(monthNode, "Dates", null)) {
							for (Node dateNode : XmlDocument.getElements(datesNode, "Date", null)) {
								String dateName = XmlDocument.getAttributeValue(dateNode, "Name");
								Date date = new Date(dateName);
								month.getDates().add(date);
								for (Node itemBoughtNode : XmlDocument.getElements(dateNode, "ItemBought", null)) {
									String itemName = XmlDocument.getAttributeValue(itemBoughtNode, "Name");
									String vendorName = XmlDocument.getAttributeValue(itemBoughtNode, "Vendor");
									String noOfPockets = XmlDocument.getAttributeValue(itemBoughtNode, "NoOfPockets");
									String totalInKgs = XmlDocument.getAttributeValue(itemBoughtNode, "TotalInKGs");
									String ratePerKg = XmlDocument.getAttributeValue(itemBoughtNode, "RatePerKG");
									String miss = XmlDocument.getAttributeValue(itemBoughtNode, "Miscellaneous");
									String unloadingCharges = XmlDocument.getAttributeValue(itemBoughtNode, "UnloadingCharges");
									ItemBought itemBought = new ItemBought(itemName, vendorName, noOfPockets, totalInKgs,
											ratePerKg, miss, unloadingCharges, memberName);
									date.getItemsBought().add(itemBought);
								}
							}
						}
					}
				} else {
					
					for (Node yearNode : XmlDocument.getElements(yearsNode, "Year", null)) {
						String yearName = XmlDocument.getAttributeValue(yearNode, "Name");
						Year year = new Year(yearName);
						member.getYears().add(year);
						monthsNode = testSetData.getNode(yearNode, "Months", null);
						for (Node monthNode : XmlDocument.getElements(monthsNode, "Month", null)) {
							String monthName = XmlDocument.getAttributeValue(monthNode, "Name");
							Month month = new Month(monthName);
							year.getMonths().add(month);
							Node datesNode = testSetData.getNode(monthNode, "Dates", null);
							for (Node dateNode : XmlDocument.getElements(datesNode, "Date", null)) {
								String dateName = XmlDocument.getAttributeValue(dateNode, "Name");
								Date date = new Date(dateName);
								month.getDates().add(date);
								for (Node itemBoughtNode : XmlDocument.getElements(dateNode, "ItemBought", null)) {
									String itemName = XmlDocument.getAttributeValue(itemBoughtNode, "Name");
									String vendorName = XmlDocument.getAttributeValue(itemBoughtNode, "Vendor");
									String noOfPockets = XmlDocument.getAttributeValue(itemBoughtNode, "NoOfPockets");
									String totalInKgs = XmlDocument.getAttributeValue(itemBoughtNode, "TotalInKGs");
									String ratePerKg = XmlDocument.getAttributeValue(itemBoughtNode, "RatePerKG");
									String miss = XmlDocument.getAttributeValue(itemBoughtNode, "Miscellaneous");
									String unloadingCharges = XmlDocument.getAttributeValue(itemBoughtNode,
											"UnloadingCharges");
									ItemBought itemBought = new ItemBought(itemName, vendorName, noOfPockets, totalInKgs,
											ratePerKg, miss, unloadingCharges, memberName);
									date.getItemsBought().add(itemBought);
								}
							}
						}
						
					}
				}
				patti.getMembers().add(member);
			}
			setPattiModel(patti);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return patti;
	}

	public void createInitialModelFile(File file) {
		try {
			file.createNewFile();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element accountsNode = doc.createElement("Accounts");
			doc.appendChild(accountsNode);
			Attr setNameAtr = doc.createAttribute("Name");
			setNameAtr.setValue("ACCOUNTS");
			accountsNode.setAttributeNode(setNameAtr);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(source, result);
		} catch (Exception localException) {
		}
	}

	public void createInitialPattiModelFile(File file) {
		try {
			file.createNewFile();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element accountsNode = doc.createElement("Patti");
			doc.appendChild(accountsNode);
			Attr setNameAtr = doc.createAttribute("Name");
			setNameAtr.setValue("PATTI");
			accountsNode.setAttributeNode(setNameAtr);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(source, result);
		} catch (Exception localException) {
		}
	}

	public void saveModelToXml(String filePath) {
		try {
			Accounts accounts = getModel();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element accountsNode = doc.createElement("Accounts");
			doc.appendChild(accountsNode);
			Attr setNameAtr = doc.createAttribute("Name");
			setNameAtr.setValue(accounts.getName());
			accountsNode.setAttributeNode(setNameAtr);

			Element membersNode = doc.createElement("Members");
			accountsNode.appendChild(membersNode);
			for (Member member : accounts.getMembers()) {
				Element menberNode = doc.createElement("Member");
				membersNode.appendChild(menberNode);
				Attr nameAttr = doc.createAttribute("Name");
				nameAttr.setValue(member.getName());
				menberNode.setAttributeNode(nameAttr);
				
				Element yearsNode = doc.createElement("Years");
				menberNode.appendChild(yearsNode);
				for (Year year : member.getYears()) {
					Element yearNode = doc.createElement("Year");
					yearsNode.appendChild(yearNode);
					nameAttr = doc.createAttribute("Name");
					nameAttr.setValue(year.getName());
					yearNode.setAttributeNode(nameAttr);
					
					Element monthsNode = doc.createElement("Months");
					yearNode.appendChild(monthsNode);
					for (Month month : year.getMonths()) {
						Element monthNode = doc.createElement("Month");
						monthsNode.appendChild(monthNode);
						nameAttr = doc.createAttribute("Name");
						nameAttr.setValue(month.getName());
						monthNode.setAttributeNode(nameAttr);
						Element datesNode = doc.createElement("Dates");
						monthNode.appendChild(datesNode);
						for (Date date : month.getDates()) {
							Element dateNode = doc.createElement("Date");
							datesNode.appendChild(dateNode);
							nameAttr = doc.createAttribute("Name");
							nameAttr.setValue(date.getName());
							dateNode.setAttributeNode(nameAttr);

							for (ItemBought item : date.getItemsBought()) {
								Element itemBoughtNode = doc.createElement("ItemBought");
								dateNode.appendChild(itemBoughtNode);

								setNameAtr = doc.createAttribute("Name");
								setNameAtr.setValue(item.getName());
								itemBoughtNode.setAttributeNode(setNameAtr);

								Attr vendor = doc.createAttribute("Vendor");
								vendor.setValue(item.getVendor());
								itemBoughtNode.setAttributeNode(vendor);

								Attr noOfPoc = doc.createAttribute("NoOfPockets");
								noOfPoc.setValue(item.getNoOfPockets());
								itemBoughtNode.setAttributeNode(noOfPoc);

								Attr totalInKgs = doc.createAttribute("TotalInKGs");
								totalInKgs.setValue(item.getTotalInKg());
								itemBoughtNode.setAttributeNode(totalInKgs);

								Attr ratePerKG = doc.createAttribute("RatePerKG");
								ratePerKG.setValue(item.getRatePerKg());
								itemBoughtNode.setAttributeNode(ratePerKG);

								Attr mis = doc.createAttribute("Miscellaneous");
								mis.setValue(item.getMiscellaneous());
								itemBoughtNode.setAttributeNode(mis);

								Attr unloadingCharges = doc.createAttribute("UnloadingCharges");
								unloadingCharges.setValue(item.getUnloadingCharges());
								itemBoughtNode.setAttributeNode(unloadingCharges);

								Element itemsSoldNode = doc.createElement("ItemsSold");
								itemBoughtNode.appendChild(itemsSoldNode);
								for (ItemSold itemSold : item.getItemsSold()) {
									Element itemSoldNode = doc.createElement("ItemSold");
									itemsSoldNode.appendChild(itemSoldNode);

									setNameAtr = doc.createAttribute("Name");
									setNameAtr.setValue(itemSold.getPersonName());
									itemSoldNode.setAttributeNode(setNameAtr);

									Attr packs = doc.createAttribute("NumberOfPacks");
									packs.setValue(itemSold.getNumberOfPacks());
									itemSoldNode.setAttributeNode(packs);

									Attr kg = doc.createAttribute("Kilograms");
									kg.setValue(itemSold.getTotalKg());
									itemSoldNode.setAttributeNode(kg);

									Attr unitPrice = doc.createAttribute("Rate");
									unitPrice.setValue(itemSold.getUnitPrice());
									itemSoldNode.setAttributeNode(unitPrice);

									Attr tranMisc = doc.createAttribute("TransportAndMiscExpenses");
									tranMisc.setValue(itemSold.getTranportAndMisc());
									itemSoldNode.setAttributeNode(tranMisc);

									Attr totalPrice = doc.createAttribute("TotalPrice");
									totalPrice.setValue(itemSold.getTotalPrice());
									itemSoldNode.setAttributeNode(totalPrice);

									Attr preBal = doc.createAttribute("PreviousBalance");
									preBal.setValue(itemSold.getPreviousBal());
									itemSoldNode.setAttributeNode(preBal);

									Attr amtRec = doc.createAttribute("AmountReceived");
									amtRec.setValue(itemSold.getAmtReceived());
									itemSoldNode.setAttributeNode(amtRec);

									Attr amtBal = doc.createAttribute("AmountBalance");
									amtBal.setValue(itemSold.getAmtBalance());
									itemSoldNode.setAttributeNode(amtBal);

									Attr amtRecMode = doc.createAttribute("AmountRecMode");
									amtRecMode.setValue(itemSold.getAmtRecMode());
									itemSoldNode.setAttributeNode(amtRecMode);

									Attr underMem = doc.createAttribute("UnderMember");
									underMem.setValue(itemSold.getSoldUnderMember());
									itemSoldNode.setAttributeNode(underMem);

									Attr bill = doc.createAttribute("BillNo");
									bill.setValue(itemSold.getBillNo());
									itemSoldNode.setAttributeNode(bill);
								}
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(filePath);
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.transform(source, result);
						// ResourcesPlugin.getWorkspace().getRoot().getProject(filePath)
						// .refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
						for (IProject pro : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
							pro.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
						}
					}
					
				}

			

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void savePattiModelToXml(String filePath) {
		try {
			Patti patti = getPattiModel();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element accountsNode = doc.createElement("Patti");
			doc.appendChild(accountsNode);
			Attr setNameAtr = doc.createAttribute("Name");
			setNameAtr.setValue(patti.getName());
			accountsNode.setAttributeNode(setNameAtr);

			Element membersNode = doc.createElement("Members");
			accountsNode.appendChild(membersNode);
			for (Member member : patti.getMembers()) {
				Element menberNode = doc.createElement("Member");
				membersNode.appendChild(menberNode);
				Attr nameAttr = doc.createAttribute("Name");
				nameAttr.setValue(member.getName());
				menberNode.setAttributeNode(nameAttr);
				
				Element yearsNode = doc.createElement("Years");
				menberNode.appendChild(yearsNode);
				for (Year year : member.getYears()) {
					Element yearNode = doc.createElement("Year");
					yearsNode.appendChild(yearNode);
					nameAttr = doc.createAttribute("Name");
					nameAttr.setValue(year.getName());
					yearNode.setAttributeNode(nameAttr);
				
					
					Element monthsNode = doc.createElement("Months");
					yearNode.appendChild(monthsNode);
					for (Month month : year.getMonths()) {
						Element monthNode = doc.createElement("Month");
						monthsNode.appendChild(monthNode);
						nameAttr = doc.createAttribute("Name");
						nameAttr.setValue(month.getName());
						monthNode.setAttributeNode(nameAttr);

						Element datesNode = doc.createElement("Dates");
						monthNode.appendChild(datesNode);
						for (Date date : month.getDates()) {
							Element dateNode = doc.createElement("Date");
							datesNode.appendChild(dateNode);
							nameAttr = doc.createAttribute("Name");
							nameAttr.setValue(date.getName());
							dateNode.setAttributeNode(nameAttr);

							for (ItemBought item : date.getItemsBought()) {
								Element itemBoughtNode = doc.createElement("ItemBought");
								dateNode.appendChild(itemBoughtNode);

								setNameAtr = doc.createAttribute("Name");
								setNameAtr.setValue(item.getName());
								itemBoughtNode.setAttributeNode(setNameAtr);

								Attr vendor = doc.createAttribute("Vendor");
								vendor.setValue(item.getVendor());
								itemBoughtNode.setAttributeNode(vendor);

								Attr noOfPoc = doc.createAttribute("NoOfPockets");
								noOfPoc.setValue(item.getNoOfPockets());
								itemBoughtNode.setAttributeNode(noOfPoc);

								Attr totalInKgs = doc.createAttribute("TotalInKGs");
								totalInKgs.setValue(item.getTotalInKg());
								itemBoughtNode.setAttributeNode(totalInKgs);

								Attr ratePerKG = doc.createAttribute("RatePerKG");
								ratePerKG.setValue(item.getRatePerKg());
								itemBoughtNode.setAttributeNode(ratePerKG);

								Attr mis = doc.createAttribute("Miscellaneous");
								mis.setValue(item.getMiscellaneous());
								itemBoughtNode.setAttributeNode(mis);

								Attr unloadingCharges = doc.createAttribute("UnloadingCharges");
								unloadingCharges.setValue(item.getUnloadingCharges());
								itemBoughtNode.setAttributeNode(unloadingCharges);
							}
						}

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(filePath);
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.transform(source, result);
						// ResourcesPlugin.getWorkspace().getRoot().getProject(filePath)
						// .refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
						for (IProject pro : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
							pro.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
						}
					}
				}

				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
