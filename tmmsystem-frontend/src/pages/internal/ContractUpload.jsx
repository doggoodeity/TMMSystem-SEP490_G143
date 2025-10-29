import React, { useEffect, useMemo, useState } from 'react';
import { Container, Card, Table, Button, Modal, Form, Alert, Badge, Spinner } from 'react-bootstrap';
import Header from '../../components/common/Header';
import InternalSidebar from '../../components/common/InternalSidebar';
import { contractService } from '../../api/contractService';
import '../../styles/QuoteRequests.css';

const STATUS_LABELS = {
  DRAFT: { text: 'Chưa upload', variant: 'secondary' },
  PENDING_APPROVAL: { text: 'Đang chờ duyệt', variant: 'warning' },
  APPROVED: { text: 'Đã duyệt', variant: 'success' },
  REJECTED: { text: 'Bị từ chối', variant: 'danger' }
};

const formatCurrency = (value) => {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(value);
};

const formatDate = (value) => {
  if (!value) return '';
  try {
    return new Date(value).toLocaleDateString('vi-VN');
  } catch (error) {
    console.warn('Cannot parse date', value, error);
    return value;
  }
};

const ContractUpload = () => {
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedContract, setSelectedContract] = useState(null);
  const [orderDetails, setOrderDetails] = useState(null);
  const [file, setFile] = useState(null);
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);

  const loadContracts = async () => {
    setLoading(true);
    setError('');

    try {
      const allContracts = await contractService.getAll();
      setContracts(Array.isArray(allContracts) ? allContracts : []);
    } catch (err) {
      console.error('Failed to load contracts', err);
      setError(err.message || 'Không thể tải danh sách hợp đồng.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContracts();
  }, []);

  const actionableContracts = useMemo(() => {
    return contracts.filter((contract) => ['DRAFT', 'REJECTED', 'PENDING_APPROVAL'].includes(contract.status));
  }, [contracts]);

  const openUploadModal = async (contract) => {
    setSelectedContract(contract);
    setModalOpen(true);
    setFile(null);
    setNotes('');
    setOrderDetails(null);
    setDetailsLoading(true);

    try {
      const details = await contractService.getOrderDetails(contract.id);
      setOrderDetails(details);
    } catch (err) {
      console.error('Unable to load contract details', err);
      setError(err.message || 'Không thể tải chi tiết hợp đồng.');
    } finally {
      setDetailsLoading(false);
    }
  };

  const closeModal = () => {
    setModalOpen(false);
    setSelectedContract(null);
    setOrderDetails(null);
    setFile(null);
    setNotes('');
  };

  const handleUpload = async () => {
    if (!selectedContract) return;
    if (!file) {
      setError('Vui lòng chọn file hợp đồng đã ký.');
      return;
    }

    const saleUserId = localStorage.getItem('userId');
    if (!saleUserId) {
      setError('Không tìm thấy thông tin nhân viên kinh doanh. Vui lòng đăng nhập lại.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      const payload = {
        notes: notes.trim() || undefined,
        saleUserId: parseInt(saleUserId, 10)
      };

      if (selectedContract.status === 'REJECTED') {
        await contractService.reUploadSignedContract(selectedContract.id, file, payload);
      } else {
        await contractService.uploadSignedContract(selectedContract.id, file, payload);
      }

      setSuccess('Upload hợp đồng thành công. Hợp đồng đang chờ giám đốc phê duyệt.');
      closeModal();
      loadContracts();
    } catch (err) {
      console.error('Upload signed contract failed', err);
      setError(err.message || 'Không thể upload hợp đồng.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="customer-layout">
      <Header />
      <div className="d-flex">
        <InternalSidebar />
        <div className="flex-grow-1" style={{ backgroundColor: '#f8f9fa', minHeight: 'calc(100vh - 70px)' }}>
          <Container fluid className="p-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
              <h1 className="mb-0">Upload hợp đồng đã ký</h1>
              <div className="text-muted">
                Bạn cần upload hợp đồng cho những báo giá khách hàng đã chấp thuận.
              </div>
            </div>

            {error && (
              <Alert variant="danger" onClose={() => setError('')} dismissible>
                {error}
              </Alert>
            )}

            {success && (
              <Alert variant="success" onClose={() => setSuccess('')} dismissible>
                {success}
              </Alert>
            )}

            <Card className="shadow-sm">
              <Card.Body className="p-0">
                <Table responsive hover className="mb-0 align-middle">
                  <thead className="table-light">
                    <tr>
                      <th style={{ width: 60 }}>#</th>
                      <th style={{ width: 180 }}>Số hợp đồng</th>
                      <th style={{ width: 140 }}>Ngày hợp đồng</th>
                      <th style={{ width: 160 }}>Ngày giao hàng</th>
                      <th style={{ width: 160 }}>Trạng thái</th>
                      <th style={{ width: 160 }}>Tổng giá trị</th>
                      <th style={{ width: 200 }}>Ghi chú</th>
                      <th style={{ width: 160 }} className="text-center">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr>
                        <td colSpan={8} className="text-center py-4">
                          <Spinner animation="border" size="sm" className="me-2" /> Đang tải hợp đồng...
                        </td>
                      </tr>
                    ) : actionableContracts.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="text-center py-4 text-muted">
                          Không có hợp đồng cần upload.
                        </td>
                      </tr>
                    ) : (
                      actionableContracts.map((contract, index) => {
                        const statusConfig = STATUS_LABELS[contract.status] || STATUS_LABELS.DRAFT;
                        return (
                          <tr key={contract.id}>
                            <td>{index + 1}</td>
                            <td className="fw-semibold text-primary">{contract.contractNumber}</td>
                            <td>{formatDate(contract.contractDate)}</td>
                            <td>{formatDate(contract.deliveryDate)}</td>
                            <td>
                              <Badge bg={statusConfig.variant}>{statusConfig.text}</Badge>
                            </td>
                            <td className="text-success fw-semibold">{formatCurrency(contract.totalAmount)}</td>
                            <td>
                              {contract.directorApprovalNotes && (
                                <span className="text-danger">{contract.directorApprovalNotes}</span>
                              )}
                            </td>
                            <td className="text-center">
                              <Button
                                variant="primary"
                                size="sm"
                                onClick={() => openUploadModal(contract)}
                              >
                                {contract.status === 'REJECTED' ? 'Upload lại' : 'Upload hợp đồng'}
                              </Button>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Container>
        </div>
      </div>

      <Modal show={modalOpen} onHide={closeModal} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>
            {selectedContract?.status === 'REJECTED' ? 'Upload lại hợp đồng' : 'Upload hợp đồng đã ký'}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {detailsLoading ? (
            <div className="text-center py-4">
              <Spinner animation="border" size="sm" className="me-2" /> Đang tải chi tiết đơn hàng...
            </div>
          ) : orderDetails ? (
            <div className="mb-4">
              <div className="mb-3">
                <strong>Mã hợp đồng:</strong> {orderDetails.contractNumber}
              </div>
              <div className="mb-3">
                <strong>Khách hàng:</strong> {orderDetails.customerInfo?.customerName}
              </div>
              <Table size="sm" bordered>
                <thead className="table-light">
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  {orderDetails.orderItems?.map((item, index) => (
                    <tr key={item.productId || index}>
                      <td>{item.productName}</td>
                      <td>{item.quantity?.toLocaleString('vi-VN')}</td>
                      <td>{formatCurrency(item.unitPrice)}</td>
                      <td>{formatCurrency(item.totalPrice)}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          ) : (
            <Alert variant="warning">Không tìm thấy chi tiết đơn hàng.</Alert>
          )}

          <Form.Group className="mb-3">
            <Form.Label>File hợp đồng (PDF hoặc hình ảnh)</Form.Label>
            <Form.Control
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(event) => setFile(event.target.files?.[0] || null)}
            />
          </Form.Group>

          <Form.Group>
            <Form.Label>Ghi chú gửi Giám đốc (tuỳ chọn)</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={notes}
              placeholder="Ví dụ: Đã ký đóng dấu đầy đủ."
              onChange={(event) => setNotes(event.target.value)}
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={closeModal} disabled={submitting}>
            Hủy
          </Button>
          <Button variant="primary" onClick={handleUpload} disabled={submitting || !file}>
            {submitting ? 'Đang upload...' : 'Upload'}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default ContractUpload;
