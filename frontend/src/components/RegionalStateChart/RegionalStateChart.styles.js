import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { FONT_COLOR } from '../../constants';

const frameStyle = css`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  max-width: 100%;
  overflow-x: hidden;
`;

const Content = styled.div`
  width: 100%;
  height: 100%;
  overflow-x: auto;
`;

const CountingDate = styled.div`
  font-size: 1.2rem;
  color: ${FONT_COLOR.LIGHT_GRAY};
  margin-top: 2rem;
`;

export { frameStyle, Content, CountingDate };
