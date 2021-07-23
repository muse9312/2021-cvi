import { useState } from 'react';
import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { useSnackbar } from 'notistack';
import { RESPONSE_STATE, SNACKBAR_MESSAGE, VACCINATION } from '../../constants';
import Modal from '../Modal/Modal';
import Selection from '../Selection/Selection';
import Button from '../Button/Button';
import { Container, TextArea, ButtonWrapper, buttonStyles } from './ReviewWritingModal.styles';
import { BUTTON_SIZE_TYPE } from '../Button/Button.styles';
import { postReviewAsync } from '../../service';
import { findKey } from '../../utils';

const ReviewWritingModal = ({ getReviewList, onClickClose }) => {
  const accessToken = useSelector((state) => state.authReducer?.accessToken);
  const { enqueueSnackbar } = useSnackbar();

  const [selectedVaccine, setSelectedVaccine] = useState('모더나');
  const [content, setContent] = useState('');

  const vaccinationList = Object.values(VACCINATION);

  const createReview = async () => {
    const vaccinationType = findKey(VACCINATION, selectedVaccine);
    const data = { content, vaccinationType };
    const response = await postReviewAsync(accessToken, data);

    if (response.state === RESPONSE_STATE.FAILURE) {
      alert('리뷰 작성 실패 - createReview');

      return;
    }

    onClickClose();
    enqueueSnackbar(SNACKBAR_MESSAGE.SUCCESS_TO_CREATE_REVIEW);

    getReviewList();
  };

  return (
    <Modal showCloseButton={true} showShadow={true} onClickClose={onClickClose}>
      <Container>
        <Selection
          selectionList={vaccinationList}
          selectedItem={selectedVaccine}
          setSelectedItem={setSelectedVaccine}
        />
        <TextArea width="100%" onChange={(event) => setContent(event.target.value)} />
        <ButtonWrapper>
          <Button sizeType={BUTTON_SIZE_TYPE.LARGE} styles={buttonStyles} onClick={createReview}>
            제출하기
          </Button>
        </ButtonWrapper>
      </Container>
    </Modal>
  );
};

ReviewWritingModal.propTypes = {
  getReviewList: PropTypes.func.isRequired,
  onClickClose: PropTypes.func.isRequired,
};

export default ReviewWritingModal;