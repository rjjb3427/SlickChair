package controllers

import models._
import Decision.Decision
import BidValue._
import Role.PC_Member
import Confidence.Confidence
import Evaluation.Evaluation
import Mappers.{enumFormMapping, idFormMapping}
import play.api.data.Form
import play.api.data.Forms.{ignored, list, mapping, nonEmptyText}
import play.api.data.Mapping
import play.api.mvc.{Call, Controller}
import play.api.templates.Html
import org.joda.time.DateTime

case class BidForm(bids: List[Bid])

object Reviewing extends Controller {
  def bidFormMapping: Mapping[Bid] = mapping(
    "paperId" -> idFormMapping[Paper],
    "personId" -> ignored(newMetadata[Person]._1),
    "bid" -> enumFormMapping(BidValue),
    "metadata" -> ignored(newMetadata[Bid])
  )(Bid.apply _)(Bid.unapply _)

  def bidForm: Form[BidForm] = Form(
    mapping("bids" -> list(bidFormMapping))
    (BidForm.apply _)(BidForm.unapply _)
  )
  
  def reviewForm: Form[Review] = Form(mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> ignored(newMetadata[Person]._1),
    "confidence" -> enumFormMapping(Confidence),
    "evaluation" -> enumFormMapping(Evaluation),
    "content" -> nonEmptyText,
    "metadat" -> ignored(newMetadata[Review])
  )(Review.apply _)(Review.unapply _))
  
  def commentForm: Form[Comment] = Form(mapping(
    "paperId" -> ignored(newMetadata[Paper]._1),
    "personId" -> ignored(newMetadata[Person]._1),
    "content" -> nonEmptyText,
    "metadata" -> ignored(newMetadata[Comment])
  )(Comment.apply _)(Comment.unapply _))

  def bid = SlickAction(IsPCMember, _.pcmemberCanBid) { implicit r =>
    val bids: List[Bid] = Query(r.db) bidsOf r.user.id
    val papers: List[Paper] = Query(r.db).allPapers
    val allBids: List[Bid] = papers map { p =>
      bids.find(_.paperId == p.id) match {
        case None => Bid(p.id, r.user.id, Maybe)
        case Some(b) => b
      }
    }
    val form = bidForm fill BidForm(allBids)
    Ok(views.html.bid(form, papers.toSet, Query(r.db).allFiles.toSet, Navbar(PC_Member)))
  }

  def doBid = SlickAction(IsPCMember, _.pcmemberCanBid) { implicit r =>
    bidForm.bindFromRequest.fold(_ => (),
      form => {
        val bids = form.bids map { _ copy (personId=r.user.id) }
        r.connection insert bids
      }
    )
    Redirect(routes.Reviewing.bid) flashing Msg.pcmember.bided
  }

  def submissions = SlickAction(IsPCMember, _ => true) { implicit r =>
    submissionsImpl(routes.Reviewing.review _, Navbar(PC_Member))
  }
  
  def submissionsImpl(infoEP: Id[Paper] => Call, navbar: Html)(implicit r: SlickRequest[_]) = {
    val files: List[File] = Query(r.db).allFiles
    val papers: List[Paper] = Query(r.db).nonConflictingPapers(r.user.id)
    val decisions: List[PaperDecision] = Query(r.db).allPaperDecisions
    val indices = Query(r.db).allPaperIndices.map(_.paperId).zipWithIndex
    val indexOf: Id[Paper] => Int = paperId =>
      indices.find(_._1 == paperId).get._2
    val assigned = Query(r.db) assignedTo r.user.id map (_.id) contains _
    val rows: List[(Paper, Int, Boolean, Option[Decision], Option[File])] = papers map { paper =>
      (paper, indexOf(paper.id), assigned(paper.id), decisions.find(_.paperId == paper.id).map(_.value), paper.fileId.map(id => files.find(_.id == id).get))
    } sortBy (_._2)
    Ok(views.html.submissionlist(rows, infoEP, navbar))
  }

  def review(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _ => true) {
    implicit r =>
    val conf: Configuration = Query(r.db).configuration
    val assigned: Boolean = Query(r.db) assignedTo (r.user.id) map (_.id) contains paperId
    val notReviewed: Boolean = Query(r.db).reviewOf(r.user.id, paperId).isEmpty
    if(conf.pcmemberCanReview && assigned && notReviewed)
      Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), reviewForm, Query(r.db).paperWithId(paperId), Navbar(PC_Member))(Submitting.summaryImpl(paperId)))
    else if(conf.pcmemberCanComment)
      commentImpl(paperId, routes.Reviewing.doComment(paperId), Navbar(PC_Member))
    else
      Submitting.infoImpl(paperId, None, None, Navbar(PC_Member))
  }
  
  def doReview(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberCanReview) { 
    implicit r =>
    reviewForm.bindFromRequest.fold(
      errors => {
        val paper: Paper = Query(r.db) paperWithId paperId
        Ok(views.html.review("Submission " + Query(r.db).indexOf(paperId), errors, paper, Navbar(PC_Member))(Submitting.summaryImpl(paperId))(Msg.flash(Msg.pcmember.reviewError)))
      },
      review => {
        r.connection insert review.copy(paperId=paperId, personId=r.user.id)
        Redirect(routes.Reviewing.review(paperId)) flashing Msg.pcmember.reviewed
      }
    )
  }
  
  def commentImpl(paperId: Id[Paper], doCommentEP: Call, navbar: Html)(implicit r: SlickRequest[_]) = {
    val optionalReview: Option[Review] = Query(r.db) reviewOf (r.user.id, paperId)
    def canEdit(review: Review): Boolean = optionalReview map (_ == review) getOrElse false
    val comments = Query(r.db) commentsOn paperId map Left.apply
    val reviews = Query(r.db) reviewsHistoryOn paperId map Right.apply
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering fromLessThan (_ isAfter _)
    val commentReviews: List[Either[Comment, Review]] = (comments ::: reviews).sortBy(
      _ fold (_.updatedAt, _.updatedAt)).reverse
    Ok(views.html.comment("Submission " + Query(r.db).indexOf(paperId), commentReviews, Query(r.db).allPersons.toSet, canEdit, doCommentEP, navbar)(Submitting.summaryImpl(paperId)))
  }
  
  def doComment(paperId: Id[Paper]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberCanComment) { 
    implicit r =>
    commentForm.bindFromRequest.fold(_ => (),
      comment => r.connection insert comment.copy(paperId=paperId, personId=r.user.id))
    Redirect(routes.Reviewing.review(paperId)) flashing Msg.pcmember.commented
  }

  def editReview(paperId: Id[Paper], personId: Id[Person]) = SlickAction(NonConflictingPCMember(paperId), _.pcmemberCanReview) { implicit r =>
    reviewForm.bindFromRequest.fold(_ => (),
      review => {
        r.connection insert review.copy(paperId=paperId, personId=personId)
      }
    )
    Redirect(routes.Reviewing.review(paperId)) flashing Msg.pcmember.edited
  }
}
