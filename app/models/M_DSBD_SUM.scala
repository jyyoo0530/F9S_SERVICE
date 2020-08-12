//package models
//
//
//import org.mongodb.scala.bson.ObjectId
//import org.mongodb.scala.bson.codecs.Macros._
//import org.mongodb.scala.bson.codecs._
//import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
//import org.bson.codecs.configuration.CodecRegistry
//
//object codec_DSBD_SUM {
//  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[M_DSBD_SUM]))
//}
//
//case class M_DSBD_SUM(_id: String = (new ObjectId()).toString,
//                      userId: String,
//                      offerTypeCode: String,
//                      var cell: M_DSBD_SUM_cell
//                     ) {}
//
//case class M_DSBD_SUM_cell(polCount: Int,
//                           podCount: Int,
//                           offerNumber: String,
//                           offerChangeSeq: Int,
//                           var aggDealQty: Int,
//                           var aggLeftQty: Int,
//                           var priceValue: Int,
//                           eventTimestamp: String,
//                           referenceEventNumber: String,
//                           referenceEventChangeSeq: Int,
//                           allYn: String,
//                           offerStatus: String,
//                           carrierCount: Int,
//                           var lineItem: M_DSBD_SUM_cell_lineItem,
//                           var routeItem: M_DSBD_SUM_cell_routeItem,
//                           var carrierItem: M_DSBD_SUM_cell_carrierItem
//                          ) {}
//
//case class M_DSBD_SUM_cell_lineItem(var baseYearWeek: String,
//                                    var dealQty: Int,
//                                    var dealPrice: Int,
//                                    var dealAmt: Int,
//                                    var leftQty: Int,
//                                    var leftPrice: Int,
//                                    var leftAmt: Int,
//                                    var lineEventTimestamp: String,
//                                    var lineReferenceEventNumber: String,
//                                    var lineReferenceEventChangeSeq: Int
//                                   ) {}
//
//case class M_DSBD_SUM_cell_routeItem(polCode: String,
//                                     polName: String,
//                                     podCode: String,
//                                     podName: String
//                                    ) {}
//
//case class M_DSBD_SUM_cell_carrierItem(carrierCode: String,
//                                       carrierName: String
//                                      ) {}
//
