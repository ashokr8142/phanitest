// License Agreement for FDA MyStudies
// Copyright © 2017-2019 Harvard Pilgrim Health Care Institute (HPHCI) and its Contributors. Permission is
// hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the &quot;Software&quot;), to deal in the Software without restriction, including without
// limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
// Software, and to permit persons to whom the Software is furnished to do so, subject to the following
// conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial
// portions of the Software.
// Funding Source: Food and Drug Administration (“Funding Agency”) effective 18 September 2014 as
// Contract no. HHSF22320140030I/HHSF22301006T (the “Prime Contract”).
// THE SOFTWARE IS PROVIDED &quot;AS IS&quot;, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
// OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

import ResearchKit
import UIKit

let kConsentCompletionResultIdentifier = "ConsentCompletion"
let kMainTitle = "Thanks for providing consent for this Study"
let kSubTitle = "You can now start participating in the Study"

/// Consent Completion Step.
class ConsentCompletionStep: ORKStep {
  var mainTitle: String?
  var subTitle: String?

  // showsProgress: Displays the step numbers in navigation bar
  func showsProgress() -> Bool {
    return false
  }

}

/// Overrriding the ORKTaskResult to get customized results.
open class ConsentCompletionTaskResult: ORKResult {

  open lazy var didTapOnViewPdf: Bool = false

  open lazy var pdfData: Data = Data()

  override open var description: String {
    return "didTapOnViewPdf:\(didTapOnViewPdf)"
  }

  override open var debugDescription: String {
    return "didTapOnViewPdf:\(didTapOnViewPdf)"
  }
}

class ConsentSharePdfStepViewController: ORKStepViewController {

  // MARK: - Outlets

  @IBOutlet weak var buttonViewPdf: UIButton?  // button to Push to PdfViewer
  @IBOutlet weak var buttonNext: UIButton?  // button to take to next step
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!

  @IBOutlet weak var labelTitle: UILabel!
  @IBOutlet weak var lableDescription: UILabel!

  // MARK: - Properties

  var consentDocument: ORKConsentDocument?

  var taskResult: ConsentCompletionTaskResult = ConsentCompletionTaskResult(
    identifier: kConsentCompletionResultIdentifier
  )

  // MARK: - ORKstepView Controller Init methods

  override init(step: ORKStep?) {
    super.init(step: step)
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  deinit {
    NotificationCenter.default.removeObserver(self)
  }

  // MARK: - Overriden Methods
  override func hasNextStep() -> Bool {
    super.hasNextStep()
    return true
  }

  override func goForward() {
    super.goForward()
  }

  override var result: ORKStepResult? {
    let orkResult = super.result
    orkResult?.results = [self.taskResult]
    return orkResult
  }

  // MARK: - View controller LifeCycle

  override func viewDidLoad() {
    super.viewDidLoad()

    if let step = step as? ConsentCompletionStep {
      step.mainTitle = kMainTitle
      step.subTitle = kSubTitle
    }

    buttonViewPdf?.layer.borderColor = kUicolorForButtonBackground
    buttonNext?.layer.borderColor = kUicolorForButtonBackground

    addObservers()

    activityIndicator.startAnimating()
    // Hide views until enrollment completed
    buttonNext?.isHidden = true
    buttonViewPdf?.isHidden = true
    labelTitle.isHidden = true
    lableDescription.isHidden = true

  }

  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    NotificationCenter.default.removeObserver(
      self,
      name: NSNotification.Name(rawValue: "NotificationStudyEnrollmentCompleted"),
      object: nil
    )
  }

  // MARK: - Utils

  fileprivate func addObservers() {
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(enrollmentCompleted),
      name: NSNotification.Name(rawValue: "NotificationStudyEnrollmentCompleted"),
      object: nil
    )

    NotificationCenter.default.addObserver(
      self,
      selector: #selector(enrollmentFailed),
      name: NSNotification.Name(rawValue: "NotificationStudyEnrollmentFailed"),
      object: nil
    )
  }

  @objc private func enrollmentCompleted() {
    activityIndicator.stopAnimating()
    buttonNext?.isHidden = false
    buttonViewPdf?.isHidden = false
    labelTitle.isHidden = false
    lableDescription.isHidden = false
  }

  @objc func enrollmentFailed(notification: NSNotification) {
    let error = notification.object as? Error
    self.taskViewController?.delegate?.taskViewController(
      self.taskViewController!,
      didFinishWith: .failed,
      error: error
    )
  }

  // MARK: - Button Actions

  @IBAction func buttonActionNext(sender: UIButton?) {
    self.taskResult.didTapOnViewPdf = false
    self.goForward()
  }

  @IBAction func buttonActionViewPdf(sender: UIButton?) {

    self.addProgressIndicator()

    // Generating consentDocumentPdf
    self.consentDocument?.makePDF(
      completionHandler: { data, error in
        NSLog(
          "data: \(String(describing: data))    \n  error: \(String(describing: error))"
        )

        self.taskResult.pdfData = data!

        self.taskResult.didTapOnViewPdf = true

        // Saving ConsentPdfData
        ConsentBuilder.currentConsent?.consentResult?.consentPdfData = Data()
        ConsentBuilder.currentConsent?.consentResult?.consentPdfData = data

        self.removeProgressIndicator()
        // Navigate to next step
        self.goForward()

      })
  }
}
