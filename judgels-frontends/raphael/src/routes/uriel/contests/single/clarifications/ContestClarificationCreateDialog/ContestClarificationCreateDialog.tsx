import { Classes, Button, Dialog, Intent } from '@blueprintjs/core';
import * as React from 'react';

import ContestClarificationCreateForm, {
  ContestClarificationCreateFormData,
} from '../ContestClarificationCreateForm/ContestClarificationCreateForm';
import { ContestClarificationData } from 'modules/api/uriel/contestClarification';
import { Contest } from 'modules/api/uriel/contest';

export interface ContestClarificationCreateDialogProps {
  contest: Contest;
  problemJids: string[];
  problemAliasesMap: { [problemJid: string]: string };
  problemNamesMap: { [problemJid: string]: string };
  statementLanguage: string;
  onCreateClarification: (contestJid: string, data: ContestClarificationData) => void;
}

interface ContestClarificationCreateDialogState {
  isDialogOpen?: boolean;
}

export class ContestClarificationCreateDialog extends React.Component<
  ContestClarificationCreateDialogProps,
  ContestClarificationCreateDialogState
> {
  state: ContestClarificationCreateDialogState = {};

  render() {
    return (
      <div className="content-card__section">
        {this.renderButton()}
        {this.renderDialog()}
      </div>
    );
  }

  private renderButton = () => {
    return (
      <Button intent={Intent.PRIMARY} icon="plus" onClick={this.toggleDialog} disabled={this.state.isDialogOpen}>
        New clarification
      </Button>
    );
  };

  private toggleDialog = () => {
    this.setState(prevState => ({ isDialogOpen: !prevState.isDialogOpen }));
  };

  private renderDialog = () => {
    const { contest, problemJids, problemAliasesMap, problemNamesMap } = this.props;
    const props = {
      contestJid: contest.jid,
      problemJids,
      problemAliasesMap,
      problemNamesMap,
      renderFormComponents: this.renderDialogForm,
      onSubmit: this.createClarification,
      initialValues: {
        topicJid: contest.jid,
      },
    };
    return (
      <Dialog
        isOpen={this.state.isDialogOpen || false}
        onClose={this.toggleDialog}
        title="Submit new clarification"
        canOutsideClickClose={false}
      >
        <ContestClarificationCreateForm {...props} />
      </Dialog>
    );
  };

  private renderDialogForm = (fields: JSX.Element, submitButton: JSX.Element) => (
    <>
      <div className={Classes.DIALOG_BODY}>{fields}</div>
      <div className={Classes.DIALOG_FOOTER}>
        <div className={Classes.DIALOG_FOOTER_ACTIONS}>
          <Button text="Cancel" onClick={this.toggleDialog} />
          {submitButton}
        </div>
      </div>
    </>
  );

  private createClarification = async (data: ContestClarificationCreateFormData) => {
    await this.props.onCreateClarification(this.props.contest.jid, data);
    this.setState({ isDialogOpen: false });
  };
}
